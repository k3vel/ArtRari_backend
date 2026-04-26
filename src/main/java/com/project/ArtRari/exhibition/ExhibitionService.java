package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkMapper;
import com.project.ArtRari.artwork.ArtworkRepository;
import com.project.ArtRari.artwork.dto.ArtworkPreviewResponse;
import com.project.ArtRari.artwork.dto.ArtworkResponse;
import com.project.ArtRari.artwork.tag.TagRepository;
import com.project.ArtRari.exhibition.dto.*;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import com.project.ArtRari.user.dto.UserPreviewResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitionService {
    private final ExhibitionRepository exhibitionRepository;
    private final TagRepository tagRepository;
    private final ArtworkMapper artworkMapper;
    private final UserRepository userRepository;
    private final ArtworkRepository artworkRepository;

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

    @Transactional
    public ExhibitionResponse createExhibition(ExhibitionCreateRequest exhibitionCreateRequest) {
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User curator = userRepository.getReferenceById(udi.getId());
        Exhibition exhibition = new Exhibition();
        exhibition.setCurator(curator);
        exhibition.setTitle(exhibitionCreateRequest.title());
        exhibition.setTheme(exhibitionCreateRequest.theme());
        exhibition.setDescription(exhibitionCreateRequest.description());
        exhibition.setBackgroundUrl(exhibitionCreateRequest.backgroundUrl());
        exhibition.setStartDate(exhibitionCreateRequest.startDate());
        exhibition.setStatus(ExhibitionStatus.waiting);
        exhibition.setThumbnailUrl(exhibitionCreateRequest.thumbnailUrl());
        List<Artwork> artworks = artworkRepository.findAllById(exhibitionCreateRequest.artworkIds());
        try {
            exhibition.setArtworks(artworks);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        return mapExhibitionIntoExhibitionResponse(savedExhibition);
    }

    @Transactional
    public ExhibitionResponse updateExhibition(Long id, ExhibitionUpdateRequest exhibitionUpdateRequest) {
        Exhibition exhibition = exhibitionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!udi.getId().equals(exhibition.getCurator().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        exhibition.setTitle(exhibitionUpdateRequest.title());
        exhibition.setTheme(exhibitionUpdateRequest.theme());
        exhibition.setDescription(exhibitionUpdateRequest.description());
        exhibition.setBackgroundUrl(exhibitionUpdateRequest.backgroundUrl());
        exhibition.setThumbnailUrl(exhibitionUpdateRequest.thumbnailUrl());
        List<Artwork> artworks = artworkRepository.findAllById(exhibitionUpdateRequest.artworkIds());
        try {
            exhibition.replaceArtworks(artworks);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        return mapExhibitionIntoExhibitionResponse(savedExhibition);
    }

    @Transactional
    public void deleteExhibition(Long id) {
        Exhibition exhibition = exhibitionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!udi.getId().equals(exhibition.getCurator().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        exhibitionRepository.delete(exhibition);
    }
}
