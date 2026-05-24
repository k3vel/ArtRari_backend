package com.project.ArtRari.purchase;

import com.project.ArtRari.purchase.dto.PurchaseHistoryResponse;
import com.project.ArtRari.purchase.dto.PurchasePreviewResponse;
import com.project.ArtRari.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;

    private List<PurchasePreviewResponse> mapPurchasesIntoPurchasePreviewResponse(List<Purchase> purchases) {
        return purchases.stream().map(p -> new PurchasePreviewResponse(
                p.getLot().getId(),
                p.getLot().getArtwork().getTitle(),
                p.getLot().getArtwork().getPhotoUrl(),
                p.getFinalPrice(),
                p.getStatus().name()
        )).collect(Collectors.toList());
    }

    public PurchaseHistoryResponse getPurchaseHistory() {
        UserDetailsImpl udi = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Purchase> purchases = purchaseRepository.findByUserId(udi.getId());
        return new PurchaseHistoryResponse(mapPurchasesIntoPurchasePreviewResponse(purchases));
    }
}
