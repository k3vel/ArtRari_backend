package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.tag.Tag;
import com.project.ArtRari.artwork.tag.TagRepository;
import com.project.ArtRari.exhibition.dto.ExhibitionPreviewResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionResponse;
import com.project.ArtRari.exhibition.dto.ExhibitionsPageResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ExhibitionService {
    private ExhibitionRepository exhibitionRepository;
    private TagRepository tagRepository;

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
        return new ExhibitionResponse(
                exhibition.getId(),
                exhibition.getTitle(),
                exhibition.getDescription(),
                exhibition.getBackgroundUrl(),
                exhibition.getStartDate(),
                exhibition.getStatus()
        );
    }

    public List<ExhibitionPreviewResponse> getTop6Preview() {
        List<Exhibition> exhibitions = exhibitionRepository.findTop6ByStatusOrderByIdDesc(ExhibitionStatus.running);
        return mapExhibitionsIntoExhibitionPreviewResponse(exhibitions);
    }

    public ExhibitionResponse getById(Long id) {
        Exhibition exhibition = exhibitionRepository.findByIdAndStatus(id, ExhibitionStatus.running).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
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
