package com.project.ArtRari.payment;

import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.payment.dto.PaymentRequest;
import com.project.ArtRari.purchase.Purchase;
import com.project.ArtRari.purchase.PurchaseRepository;
import com.project.ArtRari.purchase.PurchaseStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PurchaseRepository purchaseRepository;

    @Transactional
    public void payLot(Long lotId, PaymentRequest paymentRequest, UserDetailsImpl userDetailsImpl) {
        Purchase purchase = purchaseRepository.findByLotId(lotId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (purchase.getStatus()!=PurchaseStatus.pending_payment) {
            throw new ArtrariException(HttpStatus.CONFLICT, "Лот вже було оплачено");
        }
        if (!purchase.getUser().getId().equals(userDetailsImpl.getId())) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Цей лот не належить даному користувачу");
        }
        purchase.setStatus(PurchaseStatus.pending_shipment);
    }
}
