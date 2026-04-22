package com.project.ArtRari.lot;

import com.project.ArtRari.lot.dto.LotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lots")
@RequiredArgsConstructor
public class LotController {
    private final LotService lotService;

    @GetMapping("/{id}")
    public LotResponse getLot(@PathVariable Long id) {
        return lotService.getById(id);
    }


}
