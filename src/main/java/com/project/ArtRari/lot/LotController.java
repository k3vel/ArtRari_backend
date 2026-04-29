package com.project.ArtRari.lot;

import com.project.ArtRari.lot.dto.LotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
