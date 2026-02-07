package com.dws.trade.tradestore.service;

import com.dws.trade.tradestore.dto.TradeRequestDTO;
import com.dws.trade.tradestore.dto.TradeResponseDTO;
import com.dws.trade.tradestore.exception.TradeMaturityException;
import com.dws.trade.tradestore.exception.TradeNotFoundException;
import com.dws.trade.tradestore.exception.TradeVersionException;
import com.dws.trade.tradestore.model.Trade;
import com.dws.trade.tradestore.model.TradeStatus;
import com.dws.trade.tradestore.repository.TradeAuditRepository;
import com.dws.trade.tradestore.repository.TradeRepository;
import com.dws.trade.tradestore.validator.TradeMaturityValidator;
import com.dws.trade.tradestore.validator.TradeVersionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TradeService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeAuditRepository tradeAuditRepository;

    @Mock
    private TradeVersionValidator versionValidator;

    @Mock
    private TradeMaturityValidator maturityValidator;

    @Mock
    private TradeProducerService tradeProducerService;

    @InjectMocks
    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(tradeRepository, tradeAuditRepository, versionValidator, maturityValidator);
    }

    @Test
    void testSaveTrade_NewTrade_ShouldCreateSuccessfully() {
        // Arrange
        TradeRequestDTO requestDTO = TradeRequestDTO.builder()
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().plusDays(10))
            .build();

        when(tradeRepository.findByTradeId("T1")).thenReturn(Optional.empty());

        Trade savedTrade = Trade.builder()
            .id(1L)
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().plusDays(10))
            .createdDate(LocalDateTime.now())
            .status(TradeStatus.ACTIVE)
            .expired(false)
            .build();

        when(tradeRepository.save(any(Trade.class))).thenReturn(savedTrade);

        // Act
        TradeResponseDTO response = tradeService.saveTrade(requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals("T1", response.getTradeId());
        assertEquals(1, response.getVersion());
        verify(tradeRepository, times(1)).save(any(Trade.class));
        verify(tradeAuditRepository, times(1)).save(any());
        verify(tradeProducerService, times(1)).sendTradeEvent(any(Trade.class), eq("CREATE"));
    }

    @Test
    void testSaveTrade_InvalidMaturityDate_ShouldThrow() {
        // Arrange
        TradeRequestDTO requestDTO = TradeRequestDTO.builder()
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().minusDays(1))
            .build();

        doThrow(new TradeMaturityException("Invalid maturity date"))
            .when(maturityValidator).validateMaturityDate(any(LocalDate.class), anyString());

        // Act & Assert
        assertThrows(TradeMaturityException.class, () -> tradeService.saveTrade(requestDTO));
    }

    @Test
    void testSaveTrade_LowerVersion_ShouldThrow() {
        // Arrange
        TradeRequestDTO requestDTO = TradeRequestDTO.builder()
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().plusDays(10))
            .build();

        Trade existingTrade = Trade.builder()
            .tradeId("T1")
            .version(2)
            .build();

        when(tradeRepository.findByTradeId("T1")).thenReturn(Optional.of(existingTrade));
        doThrow(new TradeVersionException("Lower version rejected"))
            .when(versionValidator).validateVersion(any(Optional.class), anyInt(), anyString());

        // Act & Assert
        assertThrows(TradeVersionException.class, () -> tradeService.saveTrade(requestDTO));
    }

    @Test
    void testGetTradeByTradeId_ExistingTrade_ShouldReturn() {
        // Arrange
        Trade trade = Trade.builder()
            .id(1L)
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().plusDays(10))
            .createdDate(LocalDateTime.now())
            .status(TradeStatus.ACTIVE)
            .expired(false)
            .build();

        when(tradeRepository.findByTradeId("T1")).thenReturn(Optional.of(trade));

        // Act
        TradeResponseDTO response = tradeService.getTradeByTradeId("T1");

        // Assert
        assertNotNull(response);
        assertEquals("T1", response.getTradeId());
        verify(tradeRepository, times(1)).findByTradeId("T1");
    }

    @Test
    void testGetTradeByTradeId_NonExistingTrade_ShouldThrow() {
        // Arrange
        when(tradeRepository.findByTradeId("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TradeNotFoundException.class,
            () -> tradeService.getTradeByTradeId("INVALID"));
    }

    @Test
    void testMarkTradeAsExpired_ShouldSuccess() {
        // Arrange
        Trade trade = Trade.builder()
            .id(1L)
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().minusDays(1))
            .createdDate(LocalDateTime.now())
            .status(TradeStatus.ACTIVE)
            .expired(false)
            .build();

        Trade expiredTrade = Trade.builder()
            .id(1L)
            .tradeId("T1")
            .version(1)
            .counterPartyId("CP-1")
            .bookId("B1")
            .maturityDate(LocalDate.now().minusDays(1))
            .createdDate(LocalDateTime.now())
            .status(TradeStatus.EXPIRED)
            .expired(true)
            .expiryDate(LocalDate.now())
            .build();

        when(tradeRepository.findByTradeId("T1")).thenReturn(Optional.of(trade));
        when(tradeRepository.save(any(Trade.class))).thenReturn(expiredTrade);

        // Act
        TradeResponseDTO response = tradeService.markTradeAsExpired("T1");

        // Assert
        assertNotNull(response);
        assertTrue(response.getExpired());
        assertEquals("EXPIRED", response.getStatus());
        verify(tradeRepository, times(1)).save(any(Trade.class));
    }
}
