package com.project.ArtRari.bid;

import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.auction.AuctionStatus;
import com.project.ArtRari.bid.dto.BidPreviewResponse;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidService {
    private final BidRepository bidRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;

    public List<BidPreviewResponse> getBidPreviews(Long lotId) {
        List<Bid> bids = bidRepository.findByLotId(lotId);
        Long userId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl udi) {
            userId = udi.getId();
        }
        final Long finalUserId = userId;
        return bids.stream().map(b -> new BidPreviewResponse(
                b.getUser().getId().equals(finalUserId) ? "Ви" : "Невідомий поціновувач мистецтва",
                b.getAmount(),
                b.getCreatedAt()
        )).sorted(Comparator.comparing(BidPreviewResponse::createdAt).reversed()).collect(Collectors.toList());
    }

    @Transactional
    public void placeBid(Long lotId, BigDecimal amount) {
        Lot lot = lotRepository.findByIdForUpdate(lotId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        Auction auction = lot.getAuction();
        BigDecimal step = auction.getStep();
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long sellerId = lot.getArtwork().getUser().getId();
        if (sellerId.equals(udi.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ви не можете робити ставку на свій лот.");
        }
        if (!auction.getStatus().equals(AuctionStatus.active))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Аукціон вже закінчився.");
        if (!lot.getStatus().equals(LotStatus.available))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Цей лот не доступний.");
        if (amount.compareTo(lot.getCurrentPrice().add(step)) < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ставка є замалою.");

        if (Duration.between(Instant.now(), lot.getEndDate()).toMinutes() < 10) {
            lot.setEndDate(Instant.now().plusSeconds(600));
        }
        if (Duration.between(Instant.now(), auction.getEndDate()).toMinutes() < 10) {
            auction.setEndDate(Instant.now().plusSeconds(600)); //todo блокировка для аукциона
        }

        lot.setCurrentPrice(amount);
        Bid bid = new Bid();
        bid.setUser(userRepository.getReferenceById(udi.getId()));
        bid.setLot(lot);
        bid.setAmount(amount);
        bid.setCreatedAt(Instant.now());
        bid.setWin(false);
        bidRepository.save(bid);
    }

}
