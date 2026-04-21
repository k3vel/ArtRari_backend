package com.project.ArtRari.lot;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.auction.Auction;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "lot")
@Data
public class Lot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "work_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Artwork artwork;

    @JoinColumn(name = "auction_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Auction auction;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @Column(name = "end_date")
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private LotStatus status;
}
