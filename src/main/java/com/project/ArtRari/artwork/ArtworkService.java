package com.project.ArtRari.artwork;

import com.project.ArtRari.artwork.dto.ArtworkCreateRequest;
import com.project.ArtRari.artwork.dto.ArtworkResponse;
import com.project.ArtRari.artwork.dto.ArtworkUpdateRequest;
import com.project.ArtRari.artwork.tag.Tag;
import com.project.ArtRari.artwork.tag.TagRepository;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public ArtworkResponse getById(Long id) {
        Artwork artwork = artworkRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        return artworkMapper.toArtworkResponse(artwork);
    }

    @Transactional
    public ArtworkResponse addArtwork(ArtworkCreateRequest artworkCreateRequest) {
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(udi.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<Tag> tags = artworkCreateRequest.tags().stream().map(t -> tagRepository.getReferenceById(t)).toList();
        Artwork artwork = new Artwork();
        artwork.setUser(user);
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
    public ArtworkResponse updateArtwork(Long id, ArtworkUpdateRequest artworkUpdateRequest) {
        Artwork artwork = artworkRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!udi.getId().equals(artwork.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
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
    public void deleteArtwork(Long id) {
        Artwork artwork = artworkRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!udi.getId().equals(artwork.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        artworkRepository.delete(artwork);
    }
}
