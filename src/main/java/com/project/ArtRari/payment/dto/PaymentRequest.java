package com.project.ArtRari.payment.dto;
import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
        @NotBlank(message = "Номер карти не може бути порожнім")
        String card,
        @NotBlank(message = "CVV є обов'язковим")
        String cvv,
        @NotBlank(message = "Термін дії є обов'язковим")
        String expDate
) {}
