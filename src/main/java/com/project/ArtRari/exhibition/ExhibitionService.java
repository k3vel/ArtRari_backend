package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkRepository;
import com.project.ArtRari.tag.TagRepository;
import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.exhibition.dto.*;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExhibitionService {
    private final ExhibitionRepository exhibitionRepository;
    private final TagRepository tagRepository;
    private final ExhibitionMapper exhibitionMapper;
    private final UserRepository userRepository;
    private final ArtworkRepository artworkRepository;

    @Value("${app.pagination.default-size}")
    private int pageSize;

    @Cacheable("top_exhibitions")
    public List<ExhibitionPreviewResponse> getTop6Preview() {
        List<Exhibition> exhibitions = exhibitionRepository.findTop6ByStatusOrderByIdDesc(ExhibitionStatus.running);
        return exhibitions.stream().map(
                exhibitionMapper::toExhibitionPreviewResponse
        ).collect(Collectors.toList());
    }

    public ExhibitionResponse getById(Long id) {
        Exhibition exhibition = exhibitionRepository.findByIdAndStatus(id, ExhibitionStatus.running).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        return exhibitionMapper.toExhibitionResponse(exhibition);
    }

    public PageResponse<ExhibitionPreviewResponse> getExhibitions(int page, List<String> selectedTags, String search) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("startDate").descending());
        Specification<Exhibition> spec = (root, _, cb) ->
                cb.equal(root.get("status"), ExhibitionStatus.running);
        if (search != null && !search.isBlank()) {
            spec = spec.and(ExhibitionSpecifications.hasText(search));
        }
        if (selectedTags != null && !selectedTags.isEmpty()) {
            spec = spec.and(ExhibitionSpecifications.hasTags(selectedTags));
        }
        Page<Exhibition> exhibitions = exhibitionRepository.findAll(spec, pageable);
        Page<ExhibitionPreviewResponse> eprs = exhibitions.map(exhibitionMapper::toExhibitionPreviewResponse);
        return new PageResponse<>(eprs);
    }

    @Transactional
    @CacheEvict(value = "top_exhibitions")
    public ExhibitionResponse createExhibition(ExhibitionCreateRequest request, UserDetailsImpl udi) {
        User curator = userRepository.getReferenceById(udi.getId());
        Exhibition exhibition = new Exhibition();
        exhibition.setCurator(curator);
        exhibition.setTitle(request.title());
        exhibition.setTheme(request.theme());
        exhibition.setDescription(request.description());
        exhibition.setBackgroundUrl(request.backgroundUrl());
        exhibition.setStartDate(Instant.now());
        exhibition.setStatus(ExhibitionStatus.running);
        exhibition.setThumbnailUrl(request.thumbnailUrl());
        List<Artwork> artworks = artworkRepository.findAllById(request.artworkIds());
        try {
            exhibition.setArtworks(artworks);
        } catch (IllegalStateException e) {
            throw new ArtrariException(HttpStatus.CONFLICT, e.getMessage());
        }
        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        return exhibitionMapper.toExhibitionResponse(savedExhibition);
    }

    @Transactional
    @CacheEvict(value = "top_exhibitions")
    public ExhibitionResponse updateExhibition(
            Long id,
            ExhibitionUpdateRequest request,
            UserDetailsImpl udi) {
        Exhibition exhibition = exhibitionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (!exhibition.getVersion().equals(request.version())) {
            throw new ArtrariException(HttpStatus.CONFLICT, "Дані було змінено. Будь-ласка, оновіть сторінку");
        }
        if (!udi.getId().equals(exhibition.getCurator().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (exhibition.getStatus() == ExhibitionStatus.converted_into_auction) {
            throw new ArtrariException(HttpStatus.CONFLICT,
                    "Ви не можете редагувати виставку, яку перетворено на аукціон");
        }
        exhibition.setTitle(request.title());
        exhibition.setTheme(request.theme());
        exhibition.setDescription(request.description());
        exhibition.setBackgroundUrl(request.backgroundUrl());
        exhibition.setThumbnailUrl(request.thumbnailUrl());
        List<Artwork> artworks = artworkRepository.findAllById(request.artworkIds());
        try {
            exhibition.replaceArtworks(artworks);
        } catch (IllegalStateException e) {
            throw new ArtrariException(HttpStatus.CONFLICT, e.getMessage());
        }
        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        return exhibitionMapper.toExhibitionResponse(savedExhibition);
    }

    @Transactional
    @CacheEvict(value = "top_exhibitions")
    public void deleteExhibition(Long id, UserDetailsImpl udi) {
        Exhibition exhibition = exhibitionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (!udi.getId().equals(exhibition.getCurator().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (exhibition.getStatus() == ExhibitionStatus.converted_into_auction) {
            throw new ArtrariException(HttpStatus.CONFLICT,
                    "Ви не можете видаляти виставку, яку перетворено на аукціон");
        }
        for (Artwork artwork : exhibition.getArtworks()) {
            artwork.setExhibition(null);
        }
        exhibitionRepository.delete(exhibition);
    }

    public PageResponse<ExhibitionPreviewResponse> getMyExhibitions(int page, UserDetailsImpl udi) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("startDate").descending());
        Page<Exhibition> exhibitions = exhibitionRepository.findByCuratorId(udi.getId(), pageable);
        Page<ExhibitionPreviewResponse> eprs = exhibitions.map(exhibitionMapper::toExhibitionPreviewResponse);
        return new PageResponse<>(eprs);
    }

    public ExhibitionAdvancedResponse getMyExhibitionAdvanced(Long id, UserDetailsImpl udi) {
        Exhibition exhibition = exhibitionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (!udi.getId().equals(exhibition.getCurator().getId())) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ця виставка не належить Вам");
        }
        return exhibitionMapper.toExhibitionAdvancedResponse(exhibition);
    }
}
