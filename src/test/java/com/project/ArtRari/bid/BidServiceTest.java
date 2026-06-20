package com.project.ArtRari.bid;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.bid.dto.BidPreviewResponse;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BidServiceTest {
    @Mock
    private BidRepository bidRepository;
    @Mock
    private LotRepository lotRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;
    @InjectMocks
    private BidService bidService;

    private User userMock;
    private UserDetailsImpl udiMock;
    private List<Bid> bids;
    private Lot lotMock;
    private Auction auctionMock;
    private Instant endDate;

    @BeforeEach
    void setUp() {
        udiMock = new UserDetailsImpl(1L, "John", "Doe", null, null,
                null, Role.user, false);
        userMock = new User();
        userMock.setId(1L);
        userMock.setFirstName("John");
        userMock.setLastName("Doe");

        User ownerMock = new User();
        ownerMock.setId(2L);
        ownerMock.setFirstName("Jane");
        ownerMock.setLastName("Doe");

        Artwork artworkMock = new Artwork();
        artworkMock.setId(1L);
        artworkMock.setTitle("Title");
        artworkMock.setOwner(ownerMock);

        auctionMock = new Auction();
        auctionMock.setStep(new BigDecimal("1.0"));
        endDate = Instant.now();
        auctionMock.setEndDate(endDate.plusSeconds(300));

        lotMock = new Lot();
        lotMock.setId(1L);
        lotMock.setArtwork(artworkMock);
        lotMock.setAuction(auctionMock);
        lotMock.setCurrentPrice(new BigDecimal("10.10"));

        bids = new ArrayList<>();
        Bid bid1 = new Bid();
        bid1.setId(1L);
        bid1.setUser(userMock);
        bid1.setLot(lotMock);
        bid1.setAmount(new BigDecimal("10.10"));
        bids.add(bid1);
    }

    @Test
    public void getBidPreviews_ShouldSucceed() {
        when(bidRepository.findByLotIdOrderByCreatedAtDesc(lotMock.getId())).thenReturn(bids);
        List<BidPreviewResponse> res = bidService.getBidPreviews(1L, udiMock);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(new BigDecimal("10.10"), res.get(0).amount());
    }

    @Test
    public void placeBid_ShouldThrowNotFound_WhenLotNotFound() {
        when(lotRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bidService.placeBid(1L, new BigDecimal("100"), udiMock)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    public void placeBid_ShouldThrowBadRequest_WhenWrongUser() {
        udiMock.setId(2L);
        when(lotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lotMock));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> bidService.placeBid(1L, new BigDecimal("100"), udiMock)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    public void placeBid_ShouldThrowBadRequest_WhenLotScheduled() {
        lotMock.setStatus(LotStatus.scheduled);
        when(lotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lotMock));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> bidService.placeBid(1L, new BigDecimal("100"), udiMock)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    public void placeBid_ShouldThrowBadRequest_WhenLotSold() {
        lotMock.setStatus(LotStatus.sold);
        when(lotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lotMock));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> bidService.placeBid(1L, new BigDecimal("100"), udiMock)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    public void placeBid_ShouldThrowBadRequest_WhenLotCanceled() {
        lotMock.setStatus(LotStatus.cancelled);
        when(lotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lotMock));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> bidService.placeBid(1L, new BigDecimal("100"), udiMock)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    public void placeBid_ShouldThrowBadRequest_WhenAmountIsSmall() {
        lotMock.setStatus(LotStatus.available);
        when(lotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lotMock));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> bidService.placeBid(1L, new BigDecimal("0.5"), udiMock)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    public void placeBid_ShouldThrowBadRequest_WhenLotClosed() {
        lotMock.setStatus(LotStatus.available);
        lotMock.setEndDate(Instant.now().minusSeconds(60));
        when(lotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lotMock));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> bidService.placeBid(1L, new BigDecimal("100"), udiMock)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    public void placeBid_ShouldSucceed() {
        lotMock.setStatus(LotStatus.available);
        lotMock.setEndDate(endDate.plusSeconds(60));
        when(lotRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(lotMock));
        when(userRepository.getReferenceById(1L)).thenReturn(userMock);
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> {
            Bid bid = invocation.getArgument(0);
            bid.setId(999L);
            bids.add(bid);
            return bid;
        });
        when(bidRepository.findByLotIdOrderByCreatedAtDesc(lotMock.getId())).thenReturn(bids);
        BidPreviewResponse res = bidService.placeBid(1L, new BigDecimal("100"), udiMock);

        assertNotNull(res);
        assertEquals(new BigDecimal("100"), res.amount());
        verify(bidRepository).save(any(Bid.class));
        verify(simpMessagingTemplate).convertAndSend(anyString(), any(BidPreviewResponse.class));
        long secondsToLotEnd = Duration.between(Instant.now(), lotMock.getEndDate()).toSeconds();
        long secondsToAuctionEnd = Duration.between(Instant.now(), auctionMock.getEndDate()).toSeconds();
        assertTrue(secondsToLotEnd >= 598 && secondsToLotEnd <= 600);
        assertTrue(secondsToAuctionEnd >= 598 && secondsToAuctionEnd <= 600);
    }
}
