package com.project.ArtRari.bid;

import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bid")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id")
    private Lot lot;

    private BigDecimal amount;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "is_win")
    private boolean isWin;
}
