package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkMapper;
import com.project.ArtRari.exhibition.dto.ExhibitionAdvancedResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionPreviewResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionResponse;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExhibitionMapperTest {
    private ExhibitionMapper exhibitionMapper;

    public ExhibitionMapperTest() {
        UserMapper userMapper = new UserMapper();
        exhibitionMapper = new ExhibitionMapper(new ArtworkMapper(userMapper), userMapper);
    }

    private Exhibition exhibition;
    private Artwork artwork;
    private User curator;

    @BeforeEach
    void setUp() {
        curator = new User();
        curator.setId(1L);
        curator.setFirstName("John");
        curator.setLastName("Doe");

        artwork = new Artwork();
        artwork.setId(10L);
        artwork.setTitle("Mona Lisa");
        artwork.setAuthor("Author");
        artwork.setTechnique("Technique");
        artwork.setTags(List.of());
        artwork.setStartPrice(new BigDecimal("100.00"));
        artwork.setPhotoUrl("http://photo.com");

        exhibition = new Exhibition();
        exhibition.setId(100L);
        exhibition.setTitle("Exhibition Title");
        exhibition.setTheme("Exhibition Theme");
        exhibition.setDescription("Exhibition Description");
        exhibition.setThumbnailUrl("http://thumb.com");
        exhibition.setBackgroundUrl("http://bg.com");
        exhibition.setStartDate(Instant.parse("2026-06-16T10:00:00Z"));
        exhibition.setStatus(ExhibitionStatus.running);
        exhibition.setCurator(curator);
        exhibition.setArtworks(List.of(artwork));
    }

    @Test
    void toExhibitionPreviewResponse_ShouldMapCorrectly() {
        ExhibitionPreviewResponse res = exhibitionMapper.toExhibitionPreviewResponse(exhibition);
        assertNotNull(res);
        assertEquals(100L, res.id());
        assertEquals("Exhibition Title", res.title());
        assertEquals("Exhibition Theme", res.theme());
        assertEquals("http://thumb.com", res.thumbnailUrl());
        assertEquals("running", res.status());
    }

    @Test
    void toExhibitionResponse_ShouldMapCorrectly() {
        ExhibitionResponse res = exhibitionMapper.toExhibitionResponse(exhibition);
        assertNotNull(res);
        assertEquals(100L, res.id());
        assertEquals("Exhibition Title", res.title());
        assertEquals("http://thumb.com", res.thumbnailUrl());
        assertEquals("Exhibition Description", res.description());
        assertEquals("http://bg.com", res.backgroundUrl());
        assertEquals(exhibition.getStartDate(), res.startDate());
        assertEquals("running", res.status());

        assertEquals(1, res.artworks().size());
        assertEquals(10L, res.artworks().get(0).id());
        assertEquals("Mona Lisa", res.artworks().get(0).title());

        assertEquals(1L, res.curator().id());
        assertEquals("John", res.curator().firstName());
    }

    @Test
    void toExhibitionAdvancedResponse_ShouldMapCorrectly() {
        ExhibitionAdvancedResponse res = exhibitionMapper.toExhibitionAdvancedResponse(exhibition);
        assertNotNull(res);
        assertEquals(100L, res.id());
        assertEquals("Exhibition Title", res.title());
        assertEquals("http://thumb.com", res.thumbnailUrl());
        assertEquals("Exhibition Description", res.description());
        assertEquals("http://bg.com", res.backgroundUrl());
        assertEquals(exhibition.getStartDate(), res.startDate());
        assertEquals("running", res.status());

        assertEquals(1, res.artworks().size());
        assertEquals(10L, res.artworks().get(0).id());
        assertEquals("Mona Lisa", res.artworks().get(0).title());
    }
}
