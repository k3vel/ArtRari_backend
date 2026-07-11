package com.project.ArtRari.tag;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkRepository;
import com.project.ArtRari.exception.ArtrariException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final ArtworkRepository artworkRepository;

    @Cacheable(value = "tags")
    public List<TagResponse> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return tags.stream().map(t -> new TagResponse(t.getId(), t.getName())).toList();
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public void addTag(String tagName) {
        if (tagRepository.existsByNameIgnoreCase(tagName)) {
            throw new ArtrariException(HttpStatus.CONFLICT, "Тег вже існує");
        }
        Tag tag = new Tag();
        tag.setName(tagName);
        tagRepository.save(tag);
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<Artwork> artworks = artworkRepository.findByTagsId(id);
        for (Artwork artwork : artworks) {
            artwork.getTags().remove(tag);
        }
        tagRepository.delete(tag);
    }
}
