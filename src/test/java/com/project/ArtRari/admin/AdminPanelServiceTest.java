package com.project.ArtRari.admin;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkMapper;
import com.project.ArtRari.artwork.ArtworkRepository;
import com.project.ArtRari.artwork.WorkStatus;
import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.auction.AuctionStatus;
import com.project.ArtRari.bid.Bid;
import com.project.ArtRari.bid.BidRepository;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.exhibition.ExhibitionRepository;
import com.project.ArtRari.exhibition.ExhibitionStatus;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserMapper;
import com.project.ArtRari.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminPanelServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ArtworkRepository artworkRepository;
    @Mock
    private LotRepository lotRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ExhibitionRepository exhibitionRepository;
    @Mock
    private ArtworkMapper artworkMapper;
    @InjectMocks
    private AdminPanelService adminPanelService;

    private UserDetailsImpl currentAdmin;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminPanelService, "pageSize", 10);

        currentAdmin = new UserDetailsImpl(1L, "John", "Doe", "123", "abc",
                "xyz", Role.admin, false);
    }

    @Test
    void setBanned_ShouldSucceed() {
        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setRole(Role.user);
        targetUser.setBanned(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        adminPanelService.setBanned(2L, true, currentAdmin);

        assertTrue(targetUser.isBanned());
    }

    @Test
    void setBanned_ShouldThrowForbidden_WhenBanningSelf() {
        User targetUser = new User();
        targetUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));

        ArtrariException exception = assertThrows(ArtrariException.class, () ->
                adminPanelService.setBanned(1L, true, currentAdmin)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void setBanned_ThrowsException_WhenTargetIsAdmin() {
        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setRole(Role.admin);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        ArtrariException exception = assertThrows(ArtrariException.class, () ->
                adminPanelService.setBanned(2L, true, currentAdmin)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void changeRole_ShouldSucceed() {
        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setRole(Role.user);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        adminPanelService.changeRole(2L, "curator", currentAdmin);

        assertEquals(Role.curator, targetUser.getRole());
    }

    @Test
    void changeRole_ShouldThrowForbidden_WhenChangingSelf() {
        User targetUser = new User();
        targetUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(targetUser));

        ArtrariException exception = assertThrows(ArtrariException.class, () ->
                adminPanelService.changeRole(1L, "curator", currentAdmin)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void changeRole_ShouldThrowForbidden_WhenTargetIsAdmin() {
        User targetUser = new User();
        targetUser.setId(2L);
        targetUser.setRole(Role.admin);
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        ArtrariException exception = assertThrows(ArtrariException.class, () ->
                adminPanelService.changeRole(2L, "user", currentAdmin)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void deleteArtwork_ShouldSucceed() {
        Artwork artwork = new Artwork();
        artwork.setId(10L);
        artwork.setStatus(WorkStatus.available);
        when(artworkRepository.findById(10L)).thenReturn(Optional.of(artwork));
        when(lotRepository.existsByArtworkIdAndStatusNot(10L, LotStatus.unsold)).thenReturn(false);
        adminPanelService.deleteArtwork(10L);

        verify(lotRepository).deleteByArtworkId(10L);
        verify(artworkRepository).delete(artwork);
    }

    @Test
    void deleteArtwork_ShouldThrowForbidden_WhenArtworkIsSold() {
        Artwork artwork = new Artwork();
        artwork.setId(10L);
        artwork.setStatus(WorkStatus.sold);
        when(artworkRepository.findById(10L)).thenReturn(Optional.of(artwork));

        ArtrariException exception = assertThrows(ArtrariException.class, () ->
                adminPanelService.deleteArtwork(10L)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(artworkRepository, never()).delete(any(Artwork.class));
    }

    @Test
    void deleteArtwork_ThrowsException_WhenArtworkIsOnActiveAuction() {
        Artwork artwork = new Artwork();
        artwork.setId(10L);
        artwork.setStatus(WorkStatus.available);
        when(artworkRepository.findById(10L)).thenReturn(Optional.of(artwork));
        when(lotRepository.existsByArtworkIdAndStatusNot(10L, LotStatus.unsold)).thenReturn(true);

        ArtrariException exception = assertThrows(ArtrariException.class, () ->
                adminPanelService.deleteArtwork(10L)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(lotRepository, never()).deleteByArtworkId(any());
        verify(artworkRepository, never()).delete(any(Artwork.class));
    }

    @Test
    void deleteExhibition_Success_AndUnlinksArtworks() {
        Exhibition exhibition = new Exhibition();
        exhibition.setId(5L);
        exhibition.setStatus(ExhibitionStatus.running);
        Artwork a1 = new Artwork();
        a1.setExhibition(exhibition);
        Artwork a2 = new Artwork();
        a2.setExhibition(exhibition);
        exhibition.setArtworks(java.util.List.of(a1, a2));
        when(exhibitionRepository.findById(5L)).thenReturn(Optional.of(exhibition));
        adminPanelService.deleteExhibition(5L);

        assertNull(a1.getExhibition());
        assertNull(a2.getExhibition());
        verify(exhibitionRepository, times(1)).delete(exhibition);
    }

    @Test
    void deleteExhibition_ThrowsException_WhenConvertedToAuction() {
        Exhibition exhibition = new Exhibition();
        exhibition.setId(5L);
        exhibition.setStatus(ExhibitionStatus.converted_into_auction);
        when(exhibitionRepository.findById(5L)).thenReturn(Optional.of(exhibition));

        ArtrariException exception = assertThrows(ArtrariException.class, () ->
                adminPanelService.deleteExhibition(5L)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(exhibitionRepository, never()).delete(any(Exhibition.class));
    }

    @Test
    void deleteBid_ShouldSucceed_AndRecalculatesPriceToPreviousMax() {
        Auction auction = new Auction();
        auction.setStatus(AuctionStatus.active);
        Lot lot = new Lot();
        lot.setId(5L);
        lot.setAuction(auction);
        lot.setCurrentPrice(new BigDecimal("500.0"));
        Bid bidToDelete = new Bid();
        bidToDelete.setId(100L);
        bidToDelete.setLot(lot);
        Bid previousMaxBid = new Bid();
        previousMaxBid.setAmount(new BigDecimal("300.0"));
        when(bidRepository.findWithLotAndAuctionById(100L)).thenReturn(Optional.of(bidToDelete));
        when(bidRepository.findTopByLotIdOrderByAmountDesc(5L)).thenReturn(Optional.of(previousMaxBid));
        adminPanelService.deleteBid(100L);

        verify(bidRepository, times(1)).delete(bidToDelete);
        verify(bidRepository, times(1)).flush();
        assertEquals(new BigDecimal("300.0"), lot.getCurrentPrice());
    }

    @Test
    void deleteBid_Success_AndRecalculatesPriceToStartPrice() {
        Auction auction = new Auction();
        auction.setStatus(AuctionStatus.active);
        Artwork artwork = new Artwork();
        artwork.setStartPrice(new BigDecimal("100.0"));
        Lot lot = new Lot();
        lot.setId(5L);
        lot.setAuction(auction);
        lot.setArtwork(artwork);
        Bid bidToDelete = new Bid();
        bidToDelete.setId(100L);
        bidToDelete.setLot(lot);
        when(bidRepository.findWithLotAndAuctionById(100L)).thenReturn(Optional.of(bidToDelete));
        when(bidRepository.findTopByLotIdOrderByAmountDesc(5L)).thenReturn(Optional.empty());
        adminPanelService.deleteBid(100L);

        verify(bidRepository, times(1)).delete(bidToDelete);
        assertEquals(new BigDecimal("100.0"), lot.getCurrentPrice());
    }

    @Test
    void deleteBid_ShouldThrowForbidden_WhenAuctionNotActive() {
        Auction auction = new Auction();
        auction.setStatus(AuctionStatus.finished);
        Lot lot = new Lot();
        lot.setAuction(auction);
        Bid bid = new Bid();
        bid.setId(100L);
        bid.setLot(lot);
        when(bidRepository.findWithLotAndAuctionById(100L)).thenReturn(Optional.of(bid));

        ArtrariException exception = assertThrows(ArtrariException.class, () ->
                adminPanelService.deleteBid(100L)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(bidRepository, never()).delete(any());
    }
}
