package com.project.ArtRari.artwork;

import com.project.ArtRari.artwork.dto.*;
import com.project.ArtRari.artwork.tag.Tag;
import com.project.ArtRari.artwork.tag.TagRepository;
import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    public ArtworkResponse addArtwork(ArtworkCreateRequest artworkCreateRequest, UserDetailsImpl udi) {
        User user = userRepository.findById(udi.getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        List<Tag> tags = artworkCreateRequest.tags().stream().map(t -> tagRepository.getReferenceById(t)).toList();
        Artwork artwork = new Artwork();
        artwork.setOwner(user);
        artwork.setTitle(artworkCreateRequest.title());
        artwork.setAuthor(artworkCreateRequest.author());
        artwork.setDescription(artworkCreateRequest.description());
        artwork.setTechnique(artworkCreateRequest.technique());
        artwork.setTags(tags);
        artwork.setCreationDate(artworkCreateRequest.creationDate());
        artwork.setPhotoUrl(artworkCreateRequest.photoUrl());
        artwork.setStartPrice(artworkCreateRequest.startPrice());
        artwork.setStatus(WorkStatus.available);
        Artwork savedArtwork = artworkRepository.save(artwork);
        return artworkMapper.toArtworkResponse(savedArtwork);
    }

    @Transactional
    public ArtworkResponse updateArtwork(Long id, ArtworkUpdateRequest artworkUpdateRequest, UserDetailsImpl udi) {
        Artwork artwork = artworkRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (!udi.getId().equals(artwork.getOwner().getId())) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ви не можете редагувати чужу роботу");
        }
        if (lotRepository.existsByArtworkIdAndStatusNot(artwork.getId(), LotStatus.unsold)) {
            throw new ArtrariException(HttpStatus.CONFLICT, "Ви не можете редагувати роботу, якщо вона на аукціоні");
        }
        artwork.setTitle(artworkUpdateRequest.newTitle());
        artwork.setAuthor(artworkUpdateRequest.newAuthor());
        artwork.setDescription(artworkUpdateRequest.newDescription());
        artwork.setTechnique(artworkUpdateRequest.newTechnique());
        artwork.setTags(artworkUpdateRequest.newTags().stream().map(
                t -> tagRepository.getReferenceById(t)).collect(Collectors.toList())
        );
        artwork.setCreationDate(artworkUpdateRequest.newCreationDate());
        artwork.setPhotoUrl(artworkUpdateRequest.newPhotoUrl());
        artwork.setStartPrice(artworkUpdateRequest.newStartPrice());
        Artwork savedArtwork = artworkRepository.save(artwork);
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
        artworkRepository.delete(artwork);
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
