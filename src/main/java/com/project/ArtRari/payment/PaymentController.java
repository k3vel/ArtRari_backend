package com.project.ArtRari.payment;

import com.project.ArtRari.payment.dto.PaymentRequest;
import com.project.ArtRari.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/pay/lots/{id}")
    public ResponseEntity<?> payLot(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetailsImpl udi) {
        if (udi == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        paymentService.payLot(id, request, udi);
        return ResponseEntity.ok().build();
    }
}
