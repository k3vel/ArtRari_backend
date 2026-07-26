package com.project.ArtRari.bid;

import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.auction.AuctionService;
import com.project.ArtRari.bid.dto.BidPlacedEvent;
import com.project.ArtRari.bid.dto.BidPreviewResponse;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
@Slf4j
public class BidService {
    private final BidRepository bidRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;
    private final AuctionService auctionService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    @Lazy
    private BidService bidService;

    @Value("${anti-sniper.threshold-minutes:10}")
    private long thresholdMinutes;
    @Value("${anti-sniper.extension-seconds:600}")
    private long extensionSeconds;
    @Value("${anti-sniper.shift-threshold-seconds:15}")
    private long shiftThresholdSeconds;
    @Value("${anti-sniper.max-retry-attempts:3}")
    private int maxRetryAttempts;
    @Value("${anti-sniper.retry-after-ms:50}")
    private long retryAfterMs;

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
    } //todo неоптимально

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

    //антиснайпер - оптим. блок., которая будет откатывать ставки, пытающиеся одновременно продлить время аукциона
    public BidPreviewResponse placeBid(Long lotId, BigDecimal amount, UserDetailsImpl udi) {
        for (int i = 0; i < maxRetryAttempts; i++) {
            try {
                return bidService.placeBidInternal(lotId, amount, udi);
            } catch (ObjectOptimisticLockingFailureException e) {
                try {Thread.sleep(retryAfterMs);} catch (InterruptedException _) {}
            }
        }
        log.error("Не вдається поставити ставку на лот {} після {} спроб", lotId, maxRetryAttempts);
        throw new ArtrariException(HttpStatus.INTERNAL_SERVER_ERROR, "На жаль, сервер перевантажений. Спробуйте знову");
    }

    @Transactional
    public BidPreviewResponse placeBidInternal(Long lotId, BigDecimal amount, UserDetailsImpl udi) {
        Lot lot = lotRepository.findByIdForUpdate(lotId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        Auction auction = lot.getAuction();
        BigDecimal step = auction.getStep();
        Long sellerId = lot.getArtwork().getOwner().getId();
        if (sellerId.equals(udi.getId())) {
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Ви не можете робити ставку на свій лот");
        }
        if (lot.getStatus().equals(LotStatus.scheduled)) {
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Аукціон ще не розпочався");
        }
        if (lot.getStatus().equals(LotStatus.sold))
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Цей лот вже продано");
        if (lot.getStatus().equals(LotStatus.unsold) || lot.getStatus().equals(LotStatus.cancelled))
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Цей лот вже закрито");
        if (amount.compareTo(lot.getCurrentPrice().add(step)) < 0)
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Ставка є замалою");

        Instant now = Instant.now();
        Instant targetEndDate = now.plusSeconds(extensionSeconds);
        if (lot.getEndDate().isBefore(now)) {
            throw new ArtrariException(HttpStatus.BAD_REQUEST, "Цей лот вже закрито");
        }
        if (Duration.between(now, lot.getEndDate()).toMinutes() < thresholdMinutes) {
            lot.setEndDate(targetEndDate);
        }
        if (Duration.between(now, auction.getEndDate()).toMinutes() < thresholdMinutes) {
            long shiftSeconds = Duration.between(auction.getEndDate(), targetEndDate).getSeconds();
            if (shiftSeconds > shiftThresholdSeconds) {
                auction.setEndDate(targetEndDate);
                auctionService.clearCache();
            }
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
        eventPublisher.publishEvent(new BidPlacedEvent(lotId, response));

        log.info("Створено ставку {} ({} у.о.) на лот {}", savedBid.getId(), amount, lot.getId());
        return new BidPreviewResponse(
                "Невідомий поціновувач мистецтва " + userId,
                savedBid.getAmount(),
                savedBid.getCreatedAt(),
                true
        );
    }

}
