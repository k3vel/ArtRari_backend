package com.project.ArtRari.lot;

import com.project.ArtRari.lot.dto.LotResponse;
import com.project.ArtRari.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LotService {
    private final LotRepository lotRepository;
    private final LotMapper lotMapper;

    public LotResponse getById(Long id) {
        Lot lot = lotRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Long userId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl udi) {
            userId = udi.getId();
        }
        return lotMapper.toLotResponse(lot, userId);
    }
}
