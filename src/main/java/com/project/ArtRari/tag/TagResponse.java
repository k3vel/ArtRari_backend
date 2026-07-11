package com.project.ArtRari.tag;

import java.io.Serializable;

public record TagResponse (
        Long id,
        String name
) implements Serializable{}
