package com.project.ArtRari.artwork;

import com.project.ArtRari.artwork.dto.ArtworkAdvancedPreviewResponse;
import com.project.ArtRari.artwork.dto.ArtworkPreviewResponse;
import com.project.ArtRari.artwork.dto.ArtworkResponse;
import com.project.ArtRari.artwork.dto.MyArtworkResponse;
import com.project.ArtRari.artwork.tag.Tag;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArtworkMapperTest {
    private final ArtworkMapper artworkMapper = new ArtworkMapper(new UserMapper());
    private Artwork artwork;

    @BeforeEach
    public void setUp() {
        artwork = new Artwork();
        artwork.setId(1L);
        artwork.setTitle("Mona Lisa");
        artwork.setAuthor("Leonardo");
        artwork.setDescription("Classic portrait");
        artwork.setTechnique("Oil on canvas");
        artwork.setCreationDate(LocalDate.now());
        artwork.setPhotoUrl("http://example.com/monalisa.jpg");
        artwork.setStartPrice(new BigDecimal("1000.50")); // Используем String для безопасности!
        artwork.setStatus(WorkStatus.available);

        User owner = new User();
        owner.setId(10L);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        artwork.setOwner(owner);

        Tag tag1 = new Tag();
        tag1.setId(100L);
        tag1.setName("Renaissance");

        Tag tag2 = new Tag();
        tag2.setId(101L);
        tag2.setName("Portrait");

        artwork.setTags(List.of(tag1, tag2));
    }

    @Test
    public void toArtworkResponse_ShouldMapCorrectly() {
        ArtworkResponse response = artworkMapper.toArtworkResponse(artwork);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Mona Lisa", response.title());
        assertEquals("Leonardo", response.author());
        assertEquals("Classic portrait", response.description());
        assertEquals("Oil on canvas", response.technique());
        assertEquals(artwork.getCreationDate(), response.creationDate());
        assertEquals("http://example.com/monalisa.jpg", response.photoUrl());
        assertEquals("available", response.status());

        assertEquals(10L, response.owner().id());
        assertEquals("John", response.owner().firstName());

        assertEquals(2, response.tags().size());
        assertTrue(response.tags().contains("Renaissance"));
        assertTrue(response.tags().contains("Portrait"));
    }

    @Test
    public void toArtworkPreviewResponse_ShouldMapCorrectly() {
        ArtworkPreviewResponse response = artworkMapper.toArtworkPreviewResponse(artwork);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Mona Lisa", response.title());
        assertEquals("http://example.com/monalisa.jpg", response.thumbnailUrl());

        assertEquals(2, response.tags().size());
        assertEquals("Renaissance", response.tags().get(0));
    }

    @Test
    public void toArtworkAdvancedPreviewResponse_ShouldMapCorrectly() {
        ArtworkAdvancedPreviewResponse response = artworkMapper.toArtworkAdvancedPreviewResponse(artwork);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Mona Lisa", response.title());
        assertEquals("Leonardo", response.author());
        assertEquals("Oil on canvas", response.technique());
        assertEquals("http://example.com/monalisa.jpg", response.thumbnailUrl());
        assertEquals(new BigDecimal("1000.50"), response.startPrice());

        assertEquals(2, response.tags().size());
        assertEquals("Portrait", response.tags().get(1));
    }

    @Test
    public void toMyArtworkResponse_ShouldMapCorrectly() {
        MyArtworkResponse response = artworkMapper.toMyArtworkResponse(artwork);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Mona Lisa", response.title());
        assertEquals("Leonardo", response.author());
        assertEquals("Classic portrait", response.description());
        assertEquals("Oil on canvas", response.technique());
        assertEquals(artwork.getCreationDate(), response.creationDate());
        assertEquals("http://example.com/monalisa.jpg", response.photoUrl());
        assertEquals(new BigDecimal("1000.50"), response.startPrice());
        assertEquals("available", response.status());

        assertEquals(10L, response.owner().id());

        assertEquals(2, response.tags().size());
        assertEquals(100L, response.tags().get(0).id());
        assertEquals("Renaissance", response.tags().get(0).name());
        assertEquals(101L, response.tags().get(1).id());
        assertEquals("Portrait", response.tags().get(1).name());
    }
}
