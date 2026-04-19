package com.project.ArtRari.artwork.tag;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TagService {
    private TagRepository tagRepository;

    public List<String> getAllTags() {
        return tagRepository.findAll().stream().map(tag -> tag.getName()).toList();
    }
} //todo убрать?
