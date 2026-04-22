package com.project.ArtRari.artwork.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagRepository tagRepository;

    @GetMapping
    public List<TagResponse> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return tags.stream().map(t -> new TagResponse(t.getId(), t.getName())).toList();
    }
}
