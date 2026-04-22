package com.project.ArtRari.lot;

import com.project.ArtRari.lot.dto.LotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LotService {
    private final LotRepository lotRepository;
    private final LotMapper lotMapper;

    public LotResponse getById(Long id) {
        Lot lot = lotRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return lotMapper.toLotResponse(lot);
    }
}
