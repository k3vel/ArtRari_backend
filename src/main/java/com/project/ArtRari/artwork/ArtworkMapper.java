package com.project.ArtRari.artwork;

import com.project.ArtRari.artwork.dto.ArtworkAdvancedPreviewResponse;
import com.project.ArtRari.artwork.dto.ArtworkPreviewResponse;
import com.project.ArtRari.artwork.dto.ArtworkResponse;
import com.project.ArtRari.artwork.tag.Tag;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.dto.UserPreviewResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ArtworkMapper {
    public ArtworkResponse toArtworkResponse(Artwork artwork) {
        User user = artwork.getOwner();
        UserPreviewResponse safeUser = new UserPreviewResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName()
        );
        List<Tag> tags = artwork.getTags();
        List<String> safeTags = tags.stream().map(t -> t.getName()).toList();
        return new ArtworkResponse(
                artwork.getId(),
                safeUser,
                artwork.getTitle(),
                artwork.getAuthor(),
                artwork.getDescription(),
                artwork.getTechnique(),
                safeTags,
                artwork.getCreationDate(),
                artwork.getPhotoUrl(),
                artwork.getStatus().name()
        );
    }

    public ArtworkPreviewResponse toArtworkPreviewResponse(Artwork artwork) {
        List<Tag> tags = artwork.getTags();
        List<String> safeTags = tags.stream().map(t -> t.getName()).toList();
        return new ArtworkPreviewResponse(
                artwork.getId(),
                artwork.getTitle(),
                safeTags,
                artwork.getPhotoUrl()
        );
    }

    public ArtworkAdvancedPreviewResponse toArtworkAdvancedPreviewResponse(Artwork artwork) {
        List<Tag> tags = artwork.getTags();
        List<String> safeTags = tags.stream().map(t -> t.getName()).toList();
        return new ArtworkAdvancedPreviewResponse(
                artwork.getId(),
                artwork.getTitle(),
                artwork.getAuthor(),
                safeTags,
                artwork.getTechnique(),
                artwork.getStartPrice(),
                artwork.getPhotoUrl()
        );
    }
}
