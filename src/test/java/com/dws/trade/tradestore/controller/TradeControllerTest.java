package com.dws.trade.tradestore.controller;

import com.dws.trade.tradestore.dto.TradeRequestDTO;
import com.dws.trade.tradestore.dto.TradeResponseDTO;
import com.dws.trade.tradestore.exception.GlobalExceptionHandler;
import com.dws.trade.tradestore.exception.TradeNotFoundException;
import com.dws.trade.tradestore.exception.TradeVersionException;
import com.dws.trade.tradestore.model.TradeAudit;
import com.dws.trade.tradestore.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for TradeController using MockMvc.
 */
@ExtendWith(MockitoExtension.class)
class TradeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TradeService tradeService;

    private ObjectMapper objectMapper;

    private TradeRequestDTO tradeRequestDTO;
    private TradeResponseDTO tradeResponseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(new TradeController(tradeService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

        tradeRequestDTO = TradeRequestDTO.builder()
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().plusDays(10))
            .build();

        tradeResponseDTO = TradeResponseDTO.builder()
            .id(1L)
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().plusDays(10))
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .expired(false)
            .status("ACTIVE")
            .build();
    }

    @Test
    void testSaveTrade_ValidRequest_ShouldReturn201() throws Exception {
        when(tradeService.saveTrade(any(TradeRequestDTO.class))).thenReturn(tradeResponseDTO);

        mockMvc.perform(post("/trades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(tradeRequestDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tradeId", is("T1")))
            .andExpect(jsonPath("$.version", is(1)));

        verify(tradeService, times(1)).saveTrade(any(TradeRequestDTO.class));
    }

    @Test
    void testSaveTrade_InvalidRequest_ShouldReturn400() throws Exception {
        TradeRequestDTO invalidRequest = TradeRequestDTO.builder()
            .tradeId("")  // Invalid: empty trade ID
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().plusDays(10))
            .build();

        mockMvc.perform(post("/trades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(tradeService, never()).saveTrade(any(TradeRequestDTO.class));
    }

    @Test
    void testSaveTrade_VersionConflict_ShouldReturn409() throws Exception {
        when(tradeService.saveTrade(any(TradeRequestDTO.class)))
            .thenThrow(new TradeVersionException("Lower version rejected"));

        mockMvc.perform(post("/trades")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(tradeRequestDTO)))
            .andExpect(status().isConflict());

        verify(tradeService, times(1)).saveTrade(any(TradeRequestDTO.class));
    }

    @Test
    void testGetTradeByTradeId_ExistingTrade_ShouldReturn200() throws Exception {
        when(tradeService.getTradeByTradeId("T1")).thenReturn(tradeResponseDTO);

        mockMvc.perform(get("/trades/T1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tradeId", is("T1")))
            .andExpect(jsonPath("$.version", is(1)));

        verify(tradeService, times(1)).getTradeByTradeId("T1");
    }

    @Test
    void testGetTradeByTradeId_NonExistingTrade_ShouldReturn404() throws Exception {
        when(tradeService.getTradeByTradeId("INVALID"))
            .thenThrow(new TradeNotFoundException("Trade not found"));

        mockMvc.perform(get("/trades/INVALID"))
            .andExpect(status().isNotFound());

        verify(tradeService, times(1)).getTradeByTradeId("INVALID");
    }

    @Test
    void testGetAllTrades_ShouldReturn200() throws Exception {
        List<TradeResponseDTO> trades = new ArrayList<>();
        trades.add(tradeResponseDTO);

        when(tradeService.getAllTrades()).thenReturn(trades);

        mockMvc.perform(get("/trades"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].tradeId", is("T1")));

        verify(tradeService, times(1)).getAllTrades();
    }

    @Test
    void testMarkTradeAsExpired_ShouldReturn200() throws Exception {
        TradeResponseDTO expiredResponse = TradeResponseDTO.builder()
            .id(1L)
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().minusDays(1))
            .createdDate(LocalDateTime.now())
            .updatedDate(LocalDateTime.now())
            .expired(true)
            .expiryDate(LocalDate.now())
            .status("EXPIRED")
            .build();

        when(tradeService.markTradeAsExpired("T1")).thenReturn(expiredResponse);

        mockMvc.perform(put("/trades/T1/expire"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.expired", is(true)))
            .andExpect(jsonPath("$.status", is("EXPIRED")));

        verify(tradeService, times(1)).markTradeAsExpired("T1");
    }

    @Test
    void testGetAuditTrail_ShouldReturn200() throws Exception {
        List<TradeAudit> auditTrail = new ArrayList<>();
        TradeAudit audit = TradeAudit.builder()
            .id("1")
            .tradeId("T1")
            .version(1)
            .action("CREATE")
            .reason("Trade created")
            .auditTimestamp(LocalDateTime.now())
            .status("ACTIVE")
            .build();
        auditTrail.add(audit);

        when(tradeService.getAuditTrail("T1")).thenReturn(auditTrail);

        mockMvc.perform(get("/trades/T1/audit"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].tradeId", is("T1")))
            .andExpect(jsonPath("$[0].action", is("CREATE")));

        verify(tradeService, times(1)).getAuditTrail("T1");
    }
}




