package com.project.ArtRari.auction;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkMapper;
import com.project.ArtRari.auction.dto.AuctionPreviewResponse;
import com.project.ArtRari.auction.dto.AuctionResponse;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotMapper;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuctionMapperTest {
    private final AuctionMapper mapper;

    private Auction auction;


    public AuctionMapperTest() {
        UserMapper userMapper = new UserMapper();
        mapper = new AuctionMapper(new LotMapper(new ArtworkMapper(userMapper)), userMapper);
    }

    @BeforeEach
    public void setUp() {
        User curator = new User();
        curator.setId(1L);
        curator.setFirstName("John");
        curator.setLastName("Doe");

        Exhibition exhibition = new Exhibition();
        exhibition.setId(10L);
        exhibition.setTitle("Spring Art");
        exhibition.setTheme("Nature");
        exhibition.setDescription("A beautiful exhibition");
        exhibition.setThumbnailUrl("http://image.com/thumb.jpg");
        exhibition.setCurator(curator);

        Artwork artwork = new Artwork();
        artwork.setId(100L);
        artwork.setTitle("Mona Lisa");
        artwork.setOwner(curator);
        artwork.setTags(List.of());

        Lot lot = new Lot();
        lot.setId(500L);
        lot.setArtwork(artwork);
        lot.setCurrentPrice(new BigDecimal("100.00"));
        Instant endDate = Instant.now().plusSeconds(3600);
        lot.setEndDate(endDate);
        lot.setStatus(LotStatus.available);

        auction = new Auction();
        auction.setId(1000L);
        auction.setExhibition(exhibition);
        auction.setLots(List.of(lot));
        auction.setStatus(AuctionStatus.active);
        auction.setStartDate(Instant.now());
        auction.setEndDate(endDate);
    }

    @Test
    public void mapAuctionIntoAuctionPreviewResponse_ShouldMapCorrectly() {
        AuctionPreviewResponse response = mapper.mapAuctionIntoAuctionPreviewResponse(auction);

        assertNotNull(response);
        assertEquals(1000L, response.id());
        assertEquals("Spring Art", response.title());
        assertEquals("Nature", response.theme());
        assertEquals("A beautiful exhibition", response.description());
        assertEquals("http://image.com/thumb.jpg", response.thumbnailUrl());
        assertEquals("active", response.status());
        assertEquals(auction.getStartDate(), response.startDate());
        assertEquals(auction.getEndDate(), response.endDate());
    }

    @Test
    public void mapAuctionIntoAuctionResponse_ShouldMapCorrectly() {
        AuctionResponse response = mapper.mapAuctionIntoAuctionResponse(auction);

        assertNotNull(response);
        assertEquals(1000L, response.id());
        assertEquals("Spring Art", response.title());
        assertEquals("Nature", response.theme());
        assertEquals("A beautiful exhibition", response.description());
        assertEquals(auction.getStartDate(), response.startDate());
        assertEquals(auction.getEndDate(), response.endDate());
        assertEquals("active", response.status());

        assertEquals(1, response.lotPreviews().size());
        assertEquals(500L, response.lotPreviews().get(0).id());

        assertNotNull(response.curator());
        assertEquals(1L, response.curator().id());
        assertEquals("John", response.curator().firstName());
    }
}
