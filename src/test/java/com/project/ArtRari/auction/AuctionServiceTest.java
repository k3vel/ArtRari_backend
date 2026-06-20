package com.project.ArtRari.auction;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.tag.Tag;
import com.project.ArtRari.artwork.tag.TagRepository;
import com.project.ArtRari.auction.dto.*;
import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.exhibition.ExhibitionRepository;
import com.project.ArtRari.exhibition.ExhibitionStatus;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionServiceTest {
    @Mock
    private AuctionRepository auctionRepository;
    @Mock
    private AuctionMapper auctionMapper;
    @Mock
    private ExhibitionRepository exhibitionRepository;

    @InjectMocks
    private AuctionService auctionService;

    private UserDetailsImpl testUdi;
    private User curator;
    private Exhibition exhibition;
    private Auction auction;
    private Artwork artwork;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(auctionService, "pageSize", 10);

        testUdi = new UserDetailsImpl(1L, "John", "Doe", "mail", "pass",
                "123", Role.curator, false);

        curator = new User();
        curator.setId(1L);

        artwork = new Artwork();
        artwork.setId(10L);
        artwork.setStartPrice(new BigDecimal("100.00"));

        exhibition = new Exhibition();
        exhibition.setId(100L);
        exhibition.setCurator(curator);
        exhibition.setStatus(ExhibitionStatus.running);
        exhibition.setArtworks(List.of(artwork));

        auction = new Auction();
        auction.setId(1000L);
        auction.setExhibition(exhibition);
        auction.setStatus(AuctionStatus.scheduled);
        auction.setStartDate(Instant.now().plus(1, ChronoUnit.DAYS));
        auction.setEndDate(Instant.now().plus(2, ChronoUnit.DAYS));
    }

    @Test
    void getById_ShouldReturnAuction() {
        when(auctionRepository.findById(1000L)).thenReturn(Optional.of(auction));
        AuctionResponse mockResponse = mock(AuctionResponse.class);
        when(auctionMapper.mapAuctionIntoAuctionResponse(auction)).thenReturn(mockResponse);
        AuctionResponse result = auctionService.getById(1000L);

        assertNotNull(result);
        verify(auctionRepository).findById(1000L);
    }

    @Test
    void getById_ShouldThrow404_WhenNotFound() {
        when(auctionRepository.findById(1000L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> auctionService.getById(1000L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getAuctions_ShouldReturnPage_WhenTagsAreEmpty() {
        Page<Auction> auctionPage = new PageImpl<>(List.of(auction));
        when(auctionRepository.findByStatuses(
                eq(AuctionStatus.scheduled),
                eq(AuctionStatus.active),
                any(Instant.class),
                any(Pageable.class))).thenReturn(auctionPage);
        AuctionPreviewResponse mockPreview = mock(AuctionPreviewResponse.class);
        when(auctionMapper.mapAuctionIntoAuctionPreviewResponse(auction)).thenReturn(mockPreview);
        PageResponse<AuctionPreviewResponse> result = auctionService.getAuctions(0, new ArrayList<>());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(auctionRepository).findByStatuses(any(), any(), any(), any());
    }

    @Test
    void createAuction_ShouldCreate() {
        AuctionCreateRequest request = new AuctionCreateRequest(100L,
                Instant.now().plus(1, ChronoUnit.DAYS), new BigDecimal("50.00"),
                Instant.now().plus(2, ChronoUnit.DAYS));
        when(exhibitionRepository.findByIdAndStatus(100L, ExhibitionStatus.running)).thenReturn(Optional.of(exhibition));
        when(auctionRepository.existsByExhibitionId(100L)).thenReturn(false);
        when(auctionRepository.save(any(Auction.class))).thenAnswer(invocation -> {
            Auction saved = invocation.getArgument(0);
            saved.setId(999L);
            return saved;
        });
        AuctionResponse mockResponse = mock(AuctionResponse.class);
        when(auctionMapper.mapAuctionIntoAuctionResponse(any(Auction.class))).thenReturn(mockResponse);
        AuctionResponse result = auctionService.createAuction(request, testUdi);

        assertNotNull(result);
        ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);
        verify(auctionRepository).save(auctionCaptor.capture());
        Auction savedAuction = auctionCaptor.getValue();
        assertEquals(AuctionStatus.scheduled, savedAuction.getStatus());
        assertEquals(new BigDecimal("50.00"), savedAuction.getStep());
        assertEquals(1, savedAuction.getLots().size());
        assertEquals(LotStatus.scheduled, savedAuction.getLots().get(0).getStatus());
        assertEquals(new BigDecimal("100.00"), savedAuction.getLots().get(0).getCurrentPrice());
    }

    @Test
    void createAuction_ShouldThrowConflict_WhenAuctionAlreadyExists() {
        AuctionCreateRequest request = new AuctionCreateRequest(100L, Instant.now(),
                new BigDecimal("50.00"), Instant.now());
        when(exhibitionRepository.findByIdAndStatus(100L, ExhibitionStatus.running)).thenReturn(Optional.of(exhibition));
        when(auctionRepository.existsByExhibitionId(100L)).thenReturn(true);

        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> auctionService.createAuction(request, testUdi)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void createAuction_ShouldThrowForbidden_WhenWrongCurator() {
        UserDetailsImpl wrongUdi = new UserDetailsImpl(99L, "Bad", "Guy", "mail",
                "pass", "123", Role.curator, false);
        AuctionCreateRequest request = new AuctionCreateRequest(
                100L, Instant.now(), new BigDecimal("50.00"), Instant.now()
        );
        when(exhibitionRepository.findByIdAndStatus(100L, ExhibitionStatus.running)).thenReturn(Optional.of(exhibition));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> auctionService.createAuction(request, wrongUdi)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void updateAuction_ShouldThrowConflict_WhenAuctionIsActive() {
        auction.setStatus(AuctionStatus.active);
        when(auctionRepository.findById(1000L)).thenReturn(Optional.of(auction));
        AuctionUpdateRequest request = new AuctionUpdateRequest(Instant.now(), Instant.now(), new BigDecimal("10.0"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> auctionService.updateAuction(1000L, request, testUdi));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void updateAuction_ShouldUpdateAndSave_WhenValidRequest() {
        Instant newStart = Instant.now().plus(5, ChronoUnit.DAYS);
        Instant newEnd = Instant.now().plus(10, ChronoUnit.DAYS);
        AuctionUpdateRequest request = new AuctionUpdateRequest(newStart, newEnd, new BigDecimal("75.00"));
        when(auctionRepository.findById(1000L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(
                invocation -> invocation.getArgument(0));
        AuctionResponse mockResponse = mock(AuctionResponse.class);
        when(auctionMapper.mapAuctionIntoAuctionResponse(auction)).thenReturn(mockResponse);
        AuctionResponse result = auctionService.updateAuction(1000L, request, testUdi);

        assertNotNull(result);
        ArgumentCaptor<Auction> auctionCaptor = ArgumentCaptor.forClass(Auction.class);
        verify(auctionRepository).save(auctionCaptor.capture());
        Auction savedAuction = auctionCaptor.getValue();

        assertEquals(newStart, savedAuction.getStartDate());
        assertEquals(newEnd, savedAuction.getEndDate());
        assertEquals(new BigDecimal("75.00"), savedAuction.getStep());
    }

    @Test
    void updateAuction_ShouldThrowForbidden_WhenWrongCurator() {
        UserDetailsImpl wrongUdi = new UserDetailsImpl(99L, "Bad", "Guy", "mail",
                "pass", "123", Role.curator, false);
        AuctionUpdateRequest request = new AuctionUpdateRequest(Instant.now(), Instant.now(), new BigDecimal("10.0"));
        when(auctionRepository.findById(1000L)).thenReturn(Optional.of(auction));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> auctionService.updateAuction(1000L, request, wrongUdi));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(auctionRepository, never()).save(any());
    }

    @Test
    void deleteAuction_ShouldDelete() {
        when(auctionRepository.findById(1000L)).thenReturn(Optional.of(auction));
        auctionService.deleteAuction(1000L, testUdi);

        verify(auctionRepository).delete(auction);
        assertEquals(ExhibitionStatus.running, exhibition.getStatus());
    }

    @Test
    void openAuctions_ShouldUpdateStatuses() {
        when(auctionRepository.findAuctionsToOpen(any(Instant.class))).thenReturn(List.of(auction));
        auctionService.openAuctions();

        assertEquals(AuctionStatus.active, auction.getStatus());
        assertEquals(ExhibitionStatus.converted_into_auction, auction.getExhibition().getStatus());
    }

    @Test
    void closeAuctions_ShouldUpdateStatusToFinished() {
        when(auctionRepository.findAuctionsToClose(any(Instant.class))).thenReturn(List.of(auction));
        auctionService.closeAuctions();

        assertEquals(AuctionStatus.finished, auction.getStatus());
        verify(auctionRepository).findAuctionsToClose(any(Instant.class));
    }

    @Test
    void getMyAuctions_ShouldReturnPage() {
        Page<Auction> auctionPage = new PageImpl<>(List.of(auction));
        when(auctionRepository.findByExhibitionCuratorId(eq(1L), any(Pageable.class)))
                .thenReturn(auctionPage);
        AuctionPreviewResponse mockPreview = mock(AuctionPreviewResponse.class);
        when(auctionMapper.mapAuctionIntoAuctionPreviewResponse(auction)).thenReturn(mockPreview);
        PageResponse<AuctionPreviewResponse> result = auctionService.getMyAuctions(0, testUdi);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(auctionRepository).findByExhibitionCuratorId(eq(1L), any(Pageable.class));
    }
}
