package com.project.ArtRari.lot;

import com.project.ArtRari.lot.dto.LotWonEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class LotNotificationListener {
    private final SimpMessagingTemplate template;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLotWon(LotWonEvent event) {
        try {
            template.convertAndSendToUser(
                    event.winnerEmail(),
                    "/queue/lotwon",
                    event.lot());
        } catch (MessagingException e) {
            log.error("Помилка відправки сповіщення переможцю: {}", e.getMessage());
        }
    }
}
