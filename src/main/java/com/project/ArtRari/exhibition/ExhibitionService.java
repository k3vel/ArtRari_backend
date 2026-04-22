package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkMapper;
import com.project.ArtRari.artwork.dto.ArtworkPreviewResponse;
import com.project.ArtRari.artwork.dto.ArtworkResponse;
import com.project.ArtRari.artwork.tag.TagRepository;
import com.project.ArtRari.exhibition.dto.ExhibitionPreviewResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionsPageResponse;
import com.project.ArtRari.user.dto.UserPreviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitionService {
    private final ExhibitionRepository exhibitionRepository;
    private final TagRepository tagRepository;
    private final ArtworkMapper artworkMapper;

    private List<ExhibitionPreviewResponse> mapExhibitionsIntoExhibitionPreviewResponse(List<Exhibition> exhibitions) {
        return exhibitions.stream()
                .map(a -> new ExhibitionPreviewResponse(
                        a.getId(),
                        a.getTitle(),
                        a.getTheme(),
                        a.getThumbnailUrl()))
                .toList();
    }

    private ExhibitionResponse mapExhibitionIntoExhibitionResponse(Exhibition exhibition) {
        List<Artwork> artworks = exhibition.getArtworks();
        List<ArtworkPreviewResponse> safeArtworks = artworks.stream().map(a -> artworkMapper.toArtworkPreviewResponse(a)).toList();
        return new ExhibitionResponse(
                exhibition.getId(),
                exhibition.getTitle(),
                safeArtworks,
                exhibition.getDescription(),
                exhibition.getBackgroundUrl(),
                new UserPreviewResponse(exhibition.getCurator().getId(), exhibition.getCurator().getFullName()),
                exhibition.getStartDate(),
                exhibition.getStatus().name()
        );
    }

    public List<ExhibitionPreviewResponse> getTop6Preview() {
        List<Exhibition> exhibitions = exhibitionRepository.findTop6ByStatusOrderByIdDesc(ExhibitionStatus.running);
        return mapExhibitionsIntoExhibitionPreviewResponse(exhibitions);
    }

    public ExhibitionResponse getById(Long id) {
        Exhibition exhibition = exhibitionRepository.findByIdAndStatus(id, ExhibitionStatus.running).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        return mapExhibitionIntoExhibitionResponse(exhibition);
    }

    public ExhibitionsPageResponse getExhibitions(List<String> selectedTags, String search) {
        List<Exhibition> exhibitions;
        boolean tagsIsEmpty = selectedTags == null || selectedTags.isEmpty();
        boolean searchIsEmpty = search == null || search.isBlank();
        if (tagsIsEmpty && searchIsEmpty) {
            exhibitions = exhibitionRepository.findByStatus(ExhibitionStatus.running);
        } else if (tagsIsEmpty) {
            exhibitions = exhibitionRepository.findBySearchOnly(search);
        } else {
            exhibitions = exhibitionRepository.findBySearch(selectedTags, search);
        }
        List<ExhibitionPreviewResponse> eprs = mapExhibitionsIntoExhibitionPreviewResponse(exhibitions);
        List<String> tags = tagRepository.findAll().stream().map(tag -> tag.getName()).toList();
        return new ExhibitionsPageResponse(tags, eprs);
    }
}
