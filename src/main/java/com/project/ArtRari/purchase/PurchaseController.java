package com.project.ArtRari.purchase;

import com.project.ArtRari.purchase.dto.PurchaseHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('user')")
    public PurchaseHistoryResponse purchases() {
        return purchaseService.getPurchaseHistory();
    }


}
