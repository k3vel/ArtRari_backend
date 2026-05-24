package com.project.ArtRari.bid;

import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.auction.AuctionStatus;
import com.project.ArtRari.bid.dto.BidPreviewResponse;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BidService {
    private final BidRepository bidRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;


    private Map<Long,Integer> getAnonymousIds(Long lotId) {
        List<Bid> bids = bidRepository.findByLotIdOrderByCreatedAtDesc(lotId);
        Map<Long, Integer> anonymousIds = new HashMap<>();
        int i = 1;
        for (Bid bid : bids) {
            Long currentBidderId = bid.getUser().getId();
            if (!anonymousIds.containsKey(currentBidderId)) {
                anonymousIds.put(currentBidderId, i++);
            }
        }
        return anonymousIds;
    }

    public List<BidPreviewResponse> getBidPreviews(Long lotId, UserDetailsImpl udi) {
        List<Bid> bids = bidRepository.findByLotIdOrderByCreatedAtDesc(lotId);
        Map<Long,Integer> anonymousIds = getAnonymousIds(lotId);
        return bids.stream().map(b -> new BidPreviewResponse(
                "Невідомий поціновувач мистецтва " + anonymousIds.get(b.getUser().getId()),
                b.getAmount(),
                b.getCreatedAt(),
                udi != null && b.getUser().getId().equals(udi.getId())
        )).toList();
    }

    @Transactional
    public BidPreviewResponse placeBid(Long lotId, BigDecimal amount, UserDetailsImpl udi) {
        Lot lot = lotRepository.findByIdForUpdate(lotId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        Auction auction = lot.getAuction();
        BigDecimal step = auction.getStep();
        Long sellerId = lot.getArtwork().getOwner().getId();
        if (sellerId.equals(udi.getId())) {
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Ви не можете робити ставку на свій лот.");
        }
        if (!auction.getStatus().equals(AuctionStatus.active))
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Аукціон вже закінчився.");
        if (!lot.getStatus().equals(LotStatus.available))
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Цей лот не доступний.");
        if (amount.compareTo(lot.getCurrentPrice().add(step)) < 0)
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Ставка є замалою.");

        Instant now = Instant.now();
        if (Duration.between(now, lot.getEndDate()).toMinutes() < 10) {
            lot.setEndDate(now.plusSeconds(600));
        }
        if (Duration.between(now, auction.getEndDate()).toMinutes() < 10) {
            auction.setEndDate(now.plusSeconds(600)); //todo блокировка для аукциона
        }

        lot.setCurrentPrice(amount);
        Bid bid = new Bid();
        bid.setUser(userRepository.getReferenceById(udi.getId()));
        bid.setLot(lot);
        bid.setAmount(amount);
        bid.setCreatedAt(Instant.now());
        bid.setWin(false);
        Bid savedBid = bidRepository.save(bid);

        Integer userId = getAnonymousIds(savedBid.getLot().getId()).get(savedBid.getUser().getId());
        BidPreviewResponse response = new BidPreviewResponse(
                "Невідомий поціновувач мистецтва " + userId,
                savedBid.getAmount(),
                savedBid.getCreatedAt(),
                false
        );
        messagingTemplate.convertAndSend("/topic/lots/" + savedBid.getLot().getId() + "/bids", response);//todo ивент паблишер

        return new BidPreviewResponse(
                "Невідомий поціновувач мистецтва " + userId,
                savedBid.getAmount(),
                savedBid.getCreatedAt(),
                true
        );
    }

}
