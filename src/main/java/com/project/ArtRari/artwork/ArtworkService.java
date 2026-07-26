package com.project.ArtRari.artwork;

import com.project.ArtRari.artwork.dto.*;
import com.project.ArtRari.tag.Tag;
import com.project.ArtRari.tag.TagRepository;
import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtworkService {
    private final ArtworkRepository artworkRepository;
    private final ArtworkMapper artworkMapper;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final LotRepository lotRepository;

    @Value("${app.pagination.default-size}")
    private int pageSize;

    public ArtworkResponse getById(Long id) {
        Artwork artwork = artworkRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        return artworkMapper.toArtworkResponse(artwork);
    }

    public PageResponse<ArtworkAdvancedPreviewResponse> getAvailableArtworks(int page) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("creationDate").descending());
        Page<Artwork> artworks = artworkRepository.findByStatusAndExhibitionIsNull(WorkStatus.available, pageable);
        Page<ArtworkAdvancedPreviewResponse> artworkResponses = artworks.map(
                a -> artworkMapper.toArtworkAdvancedPreviewResponse(a)
        );
        return new PageResponse<>(artworkResponses);
    }

    @Transactional
    public ArtworkResponse addArtwork(ArtworkCreateRequest request, UserDetailsImpl udi) {
        User user = userRepository.findById(udi.getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        List<Tag> tags = request.tags().stream().map(t -> tagRepository.getReferenceById(t)).toList();
        Artwork artwork = new Artwork();
        artwork.setOwner(user);
        artwork.setTitle(request.title());
        artwork.setAuthor(request.author());
        artwork.setDescription(request.description());
        artwork.setTechnique(request.technique());
        artwork.setTags(tags);
        artwork.setCreationDate(request.creationDate());
        artwork.setPhotoUrl(request.photoUrl());
        artwork.setStartPrice(request.startPrice());
        artwork.setStatus(WorkStatus.available);
        Artwork savedArtwork = artworkRepository.save(artwork);
        log.info("Додано роботу {}", savedArtwork.getId());
        return artworkMapper.toArtworkResponse(savedArtwork);
    }

    @Transactional
    public ArtworkResponse updateArtwork(Long id, ArtworkUpdateRequest request, UserDetailsImpl udi) {
        Artwork artwork = artworkRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (!artwork.getVersion().equals(request.version())) {
            throw new ArtrariException(HttpStatus.CONFLICT, "Дані було змінено. Будь-ласка, оновіть сторінку");
        }
        if (!udi.getId().equals(artwork.getOwner().getId())) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ви не можете редагувати чужу роботу");
        }
        if (lotRepository.existsByArtworkIdAndStatusNot(artwork.getId(), LotStatus.unsold)) {
            throw new ArtrariException(HttpStatus.CONFLICT, "Ви не можете редагувати роботу, якщо вона на аукціоні");
        }
        artwork.setTitle(request.newTitle());
        artwork.setAuthor(request.newAuthor());
        artwork.setDescription(request.newDescription());
        artwork.setTechnique(request.newTechnique());
        artwork.setTags(request.newTags().stream().map(
                t -> tagRepository.getReferenceById(t)).collect(Collectors.toList())
        );
        artwork.setCreationDate(request.newCreationDate());
        artwork.setPhotoUrl(request.newPhotoUrl());
        artwork.setStartPrice(request.newStartPrice());
        Artwork savedArtwork = artworkRepository.save(artwork);
        log.info("Змінено роботу {}", savedArtwork.getId());
        return artworkMapper.toArtworkResponse(savedArtwork);
    }

    @Transactional
    public void deleteArtwork(Long id, UserDetailsImpl udi) {
        Artwork artwork = artworkRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (!udi.getId().equals(artwork.getOwner().getId())) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ви не можете видаляти чужу роботу");
        }
        if (lotRepository.existsByArtworkIdAndStatusNot(artwork.getId(), LotStatus.unsold)) {
            throw new ArtrariException(HttpStatus.CONFLICT, "Ви не можете видалити роботу, якщо вона на аукціоні");
        }
        lotRepository.deleteByArtworkId(artwork.getId());
        artworkRepository.delete(artwork);
        log.info("Видалено роботу {}", artwork.getId());
    }

    public PageResponse<ArtworkPreviewResponse> getMyArtworks(int page, UserDetailsImpl udi) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("creationDate").descending());
        Page<Artwork> artworks = artworkRepository.findByOwnerId(udi.getId(), pageable);
        Page<ArtworkPreviewResponse> artworkResponses = artworks.map(
                a -> artworkMapper.toArtworkPreviewResponse(a)
        );
        return new PageResponse<>(artworkResponses);
    }

    public MyArtworkResponse getMyArtwork(Long id, UserDetailsImpl udi) {
        Artwork artwork = artworkRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (!udi.getId().equals(artwork.getOwner().getId())) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ця робота не належить Вам");
        }
        return artworkMapper.toMyArtworkResponse(artwork);
    }
}
