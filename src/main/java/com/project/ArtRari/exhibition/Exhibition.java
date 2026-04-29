package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exhibition")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Exhibition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "exhibition", fetch = FetchType.LAZY)
    private List<Artwork> artworks = new ArrayList<>();

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

    public void setArtworks(List<Artwork> artworks) {
        if (artworks == null) return;
        for (Artwork artwork : artworks) {
            if (artwork.getExhibition() != null && !artwork.getExhibition().equals(this)) {
                throw new IllegalStateException("Artwork is already on another exhibition");
            }
            artwork.setExhibition(this);
            this.artworks.add(artwork);
        }
    }

    public void replaceArtworks(List<Artwork> artworks) {
        for (Artwork oldArtwork : this.artworks) {
            oldArtwork.setExhibition(null);
        }
        this.artworks.clear();
        setArtworks(artworks);
    }
}
//todo nullable/not null annotations