package com.project.ArtRari.lot;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.bid.Bid;
import com.project.ArtRari.bid.BidRepository;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.lot.dto.LotResponse;
import com.project.ArtRari.lot.dto.LotWonEvent;
import com.project.ArtRari.purchase.Purchase;
import com.project.ArtRari.purchase.PurchaseRepository;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LotServiceTest {
    @Mock
    private LotRepository lotRepository;
    @Mock
    private LotMapper lotMapper;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private LotService selfMock;
    @InjectMocks
    private LotService lotService;

    private UserDetailsImpl udiMock;
    private Lot lotMock;

    @BeforeEach
    void setUp() {
        udiMock = new UserDetailsImpl(1L, "John", "Doe", null, null,
                null, Role.user, false);

        ReflectionTestUtils.setField(lotService, "lotService", selfMock);

        lotMock = new Lot();
        lotMock.setId(1L);
        Artwork artwork = new Artwork();
        artwork.setExhibition(new Exhibition());
        lotMock.setArtwork(artwork);

    }

    @Test
    void getById_ShouldSucceed() {
        when(lotRepository.findWithArtworkAndAuctionById(1L)).thenReturn(Optional.of(lotMock));
        LotResponse mockResponse = mock(LotResponse.class);
        when(lotMapper.toLotResponse(lotMock, 1L)).thenReturn(mockResponse);
        LotResponse actualResponse = lotService.getById(1L, udiMock);

        assertNotNull(actualResponse);
        verify(lotMapper).toLotResponse(any(Lot.class), anyLong());
    }

    @Test
    void getById_ShouldThrow404_WhenNotFound() {
        when(lotRepository.findWithArtworkAndAuctionById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> lotService.getById(1L, udiMock)
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void closeLots_ShouldCloseAllLots() {
        Lot lot1 = new Lot();
        lot1.setId(10L);
        Lot lot2 = new Lot();
        lot2.setId(20L);
        when(lotRepository.findLotsToClose(any(Instant.class), eq(LotStatus.available)))
                .thenReturn(List.of(lot1, lot2));
        lotService.closeLots();

        verify(selfMock).closeLot(10L);
        verify(selfMock).closeLot(20L);
    }

    @Test
    void closeLot_ShouldClose() {
        when(lotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lotMock));
        Bid bid = new Bid();
        bid.setUser(new User());
        bid.setAmount(new BigDecimal("111.1"));
        when(bidRepository.findTopByLotIdOrderByAmountDesc(1L)).thenReturn(Optional.of(bid));
        lotService.closeLot(1L);

        assertEquals(LotStatus.sold, lotMock.getStatus());
        verify(purchaseRepository).save(any(Purchase.class));
        verify(eventPublisher).publishEvent(any(LotWonEvent.class));
    }

    @Test
    void closeLot_ShouldCloseAndSetUnsold() {
        when(lotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lotMock));
        when(bidRepository.findTopByLotIdOrderByAmountDesc(1L)).thenReturn(Optional.empty());
        lotService.closeLot(1L);

        assertEquals(LotStatus.unsold, lotMock.getStatus());
        assertNull(lotMock.getArtwork().getExhibition());
        verify(purchaseRepository, times(0)).save(any(Purchase.class));
        verify(eventPublisher, times(0)).publishEvent(any(LotWonEvent.class));
    }

    @Test
    void closeLot_ShouldThrowNoSuchElement_WhenLotNotFound() {
        when(lotRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> lotService.closeLot(1L)
        );
    }
}
