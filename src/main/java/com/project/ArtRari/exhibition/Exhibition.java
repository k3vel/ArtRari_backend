package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.user.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Data
@Entity
@Table(name = "exhibition")
public class Exhibition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "curator_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User curator;

    private String title;

    private String theme;

    private String description;

    @Column(name = "background_url")
    private String backgroundUrl;

    @Column(name = "start_date")
    private Instant startDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ExhibitionStatus status;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
}
//todo nullable/not null annotations