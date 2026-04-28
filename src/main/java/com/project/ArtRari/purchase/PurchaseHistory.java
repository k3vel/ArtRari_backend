package com.project.ArtRari.purchase;

import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purchase_history")
@Data
public class PurchaseHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "lot_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Lot lot;

    @Column(name = "final_price")
    private BigDecimal finalPrice;

    @Column(name = "win_date")
    private Instant winDate;

    private PurchaseStatus status;
}
