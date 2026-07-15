package com.project.ArtRari.bid;

import com.project.ArtRari.bid.dto.BidPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BidNotificationListener {
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBidPlaced(BidPlacedEvent event) {
        try {
            messagingTemplate.convertAndSend("/topic/lots/" + event.lotId() + "/bids", event.bidPreviewResponse());
        } catch (Exception e) {
            log.error("Помилка відправки WebSocket повідомлення для лота {}: {}", event.lotId(), e.getMessage());
        }
    }
}
