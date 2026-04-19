package com.project.ArtRari.artwork.tag;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tag")
@Data
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
