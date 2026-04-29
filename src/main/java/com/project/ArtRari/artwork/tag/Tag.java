package com.project.ArtRari.artwork.tag;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tag")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
