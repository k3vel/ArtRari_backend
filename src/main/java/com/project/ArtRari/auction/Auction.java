package com.project.ArtRari.auction;

import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.lot.Lot;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "auction")
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "auction", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lot> lots = new ArrayList<>();

    @JoinColumn(name = "exhibition_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Exhibition exhibition;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "auction_step")
    private BigDecimal step;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AuctionStatus status;

    @Column(name = "end_date")
    private Instant endDate;

    public void setLots(List<Lot> lots) {
        if (lots == null) return;
        for (Lot lot : lots) {
            if (lot.getAuction() != null && !lot.getAuction().equals(this)) {
                throw new IllegalStateException("Lot is already on another auction");
            }
            lot.setAuction(this);
            this.lots.add(lot);
        }
    }

    public void replaceLots(List<Lot> lots) {
        for (Lot oldLot : this.lots) {
            oldLot.setAuction(null);
        }
        this.lots.clear();
        this.setLots(lots);
    }
}
