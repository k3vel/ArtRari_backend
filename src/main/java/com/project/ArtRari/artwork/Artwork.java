package com.project.ArtRari.artwork;

import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.artwork.tag.Tag;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "work")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Artwork {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "owner_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @JoinColumn(name = "exhibition_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Exhibition exhibition;

    private String title;

    private String author;

    private String description;

    private String technique;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tag_work", // таблица связи много-к-многим
            joinColumns = @JoinColumn(name = "work_id"), // Колонка, которая смотрит на текущий класс (Work) в таблице связи
            inverseJoinColumns = @JoinColumn(name = "tag_id") // Колонка, которая смотрит на другой класс (Tag) в таблице связи
    )
    private List<Tag> tags;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "start_price")
    private BigDecimal startPrice;

    @OneToMany(mappedBy = "artwork", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Lot> lots;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private WorkStatus status;
}
