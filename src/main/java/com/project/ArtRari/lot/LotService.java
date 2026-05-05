package com.project.ArtRari.lot;

import com.project.ArtRari.artwork.WorkStatus;
import com.project.ArtRari.bid.Bid;
import com.project.ArtRari.bid.BidRepository;
import com.project.ArtRari.lot.dto.LotResponse;
import com.project.ArtRari.purchase.Purchase;
import com.project.ArtRari.purchase.PurchaseRepository;
import com.project.ArtRari.purchase.PurchaseStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LotService {
    private final LotRepository lotRepository;
    private final LotMapper lotMapper;
    private final BidRepository bidRepository;
    private final PurchaseRepository purchaseRepository;

    @Autowired
    @Lazy
    private LotService lotService;

    public LotResponse getById(Long id) {
        Lot lot = lotRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Long userId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl udi) {
            userId = udi.getId();
        }
        return lotMapper.toLotResponse(lot, userId);
    }

    public void closeLots() {
        List<Lot> lots = lotRepository.findLotsToClose(Instant.now(), LotStatus.available);
        for (Lot lot : lots) {
            lotService.closeLot(lot.getId());
        }
    }
//todo ВЕРНУТЬСЯ СЮДА
    @Transactional
    public void closeLot(Long id) {
        Lot lot = lotRepository.findByIdForUpdate(id).orElseThrow();
        Optional<Bid> winnerOpt = bidRepository.findTopByLotIdOrderByAmountDesc(lot.getId());
        if (winnerOpt.isPresent()) {
            Bid winner = winnerOpt.get();
            winner.setWin(true);
            lot.getArtwork().setStatus(WorkStatus.sold);
            lot.setStatus(LotStatus.sold);
            Purchase purchase = new Purchase();
            purchase.setUser(winner.getUser());
            purchase.setLot(lot);
            purchase.setFinalPrice(winner.getAmount());
            purchase.setWinDate(Instant.now());
            purchase.setStatus(PurchaseStatus.pending_payment);
            purchaseRepository.save(purchase);
        } else {
            lot.setStatus(LotStatus.unsold);
            lot.getArtwork().setExhibition(null);
        }
    }
}
