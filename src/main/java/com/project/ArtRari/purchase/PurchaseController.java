package com.project.ArtRari.purchase;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class PurchaseController {


    @GetMapping("/purchases")
    public String purchases() {
        return "";
    }
}
