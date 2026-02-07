package com.dws.trade.tradestore.service;

import com.dws.trade.tradestore.model.Trade;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for producing trade events to Kafka topics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.trade-events:trade-events}")
    private String tradeEventsTopic;

    @Value("${kafka.topic.trade-updates:trade-updates}")
    private String tradeUpdatesTopic;

    /**
     * Send a trade event to Kafka.
     * @param trade the trade entity
     * @param action the action performed (CREATE, UPDATE, EXPIRE, REJECT)
     */
    public void sendTradeEvent(Trade trade, String action) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("tradeId", trade.getTradeId());
            event.put("version", trade.getVersion());
            event.put("counterPartyId", trade.getCounterPartyId());
            event.put("bookId", trade.getBookId());
            event.put("maturityDate", trade.getMaturityDate().toString());
            event.put("status", trade.getStatus().name());
            event.put("action", action);
            event.put("timestamp", LocalDateTime.now().toString());
            event.put("expired", trade.getExpired());

            String eventJson = objectMapper.writeValueAsString(event);

            // Send to appropriate topic based on action
            String topic = action.equals("CREATE") ? tradeEventsTopic : tradeUpdatesTopic;
            kafkaTemplate.send(topic, trade.getTradeId(), eventJson);

            log.info("Trade event sent to Kafka - Trade: {}, Action: {}, Topic: {}",
                trade.getTradeId(), action, topic);
        } catch (Exception e) {
            log.error("Failed to send trade event to Kafka for trade: {}", trade.getTradeId(), e);
            throw new RuntimeException("Failed to publish trade event", e);
        }
    }
}
