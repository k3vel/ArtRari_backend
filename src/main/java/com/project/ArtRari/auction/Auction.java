package com.project.ArtRari.auction;

import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auction")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
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

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
        for (Lot lot : lots) {
            lot.setEndDate(endDate);
        }
    }

    public void setLotsStatuses(LotStatus status) {
        for (Lot lot : this.lots) {
            lot.setStatus(status);
        }
    }
}
