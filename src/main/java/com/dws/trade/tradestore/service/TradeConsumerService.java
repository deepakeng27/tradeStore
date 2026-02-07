package com.dws.trade.tradestore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * Service for consuming trade events from Kafka topics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeConsumerService {

    private final ObjectMapper objectMapper;

    /**
     * Listener for trade events from Kafka.
     * @param message the trade event message
     */
    @KafkaListener(topics = "${kafka.topic.trade-events:trade-events}",
                   groupId = "${spring.kafka.consumer.group-id:trade-store-group}")
    public void consumeTradeEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("Trade event consumed - Trade ID: {}, Action: {}",
                event.get("tradeId"), event.get("action"));
            // Process the event as needed
        } catch (Exception e) {
            log.error("Error processing trade event: {}", message, e);
        }
    }

    /**
     * Listener for trade updates from Kafka.
     * @param message the trade update message
     */
    @KafkaListener(topics = "${kafka.topic.trade-updates:trade-updates}",
                   groupId = "${spring.kafka.consumer.group-id:trade-store-group}")
    public void consumeTradeUpdate(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            log.info("Trade update consumed - Trade ID: {}, Action: {}",
                event.get("tradeId"), event.get("action"));
            // Process the update as needed
        } catch (Exception e) {
            log.error("Error processing trade update: {}", message, e);
        }
    }
}
