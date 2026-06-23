package com.project.ArtRari.lot;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkMapper;
import com.project.ArtRari.artwork.dto.ArtworkPreviewResponse;
import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.lot.dto.LotPreviewResponse;
import com.project.ArtRari.lot.dto.LotResponse;
import com.project.ArtRari.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LotMapperTest {
    @Mock
    private ArtworkMapper artworkMapper;
    @InjectMocks
    private LotMapper lotMapper;

    private Lot lot;

    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setId(1L);

        Artwork artwork = new Artwork();
        artwork.setId(1L);
        artwork.setOwner(user);
        artwork.setStartPrice(new BigDecimal("1000.50"));

        Auction auction = new Auction();
        auction.setStep(new BigDecimal("1.0"));

        lot = new Lot();
        lot.setId(1L);
        lot.setArtwork(artwork);
        lot.setAuction(auction);
        lot.setCurrentPrice(new BigDecimal("1000.50"));
        lot.setEndDate(Instant.now().plusSeconds(600));
        lot.setStatus(LotStatus.available);
    }

    @Test
    public void toLotResponse() {
        LotResponse lotResponse = lotMapper.toLotResponse(lot, 2L);

        assertNotNull(lotResponse);
        assertEquals(1L, lotResponse.id());
        assertEquals(new BigDecimal("1000.50"), lotResponse.startPrice());
        assertEquals(new BigDecimal("1000.50"), lotResponse.currentPrice());
        assertEquals(new BigDecimal("1.0"), lotResponse.step());
        assertEquals(lot.getEndDate(), lotResponse.endDate());
        assertEquals("available", lotResponse.status());
        assertFalse(lotResponse.isMyLot());
    }

    @Test
    public void toLotPreviewResponse() {
        ArtworkPreviewResponse mockPreview = new ArtworkPreviewResponse(
                100L, "Mona Lisa", List.of(), "https://photo.url"
        );
        when(artworkMapper.toArtworkPreviewResponse(lot.getArtwork())).thenReturn(mockPreview);
        LotPreviewResponse response = lotMapper.toLotPreviewResponse(lot);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(new BigDecimal("1000.50"), response.currentPrice());
        assertEquals(lot.getEndDate(), response.endDate());
        assertEquals("available", response.status());
    }
}
