package com.Conversion.de.Devises.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse de conversion de devises")
public class ConversionResponse {

    @Schema(description = "Code ISO 4217 de la devise source",
            example = "USD")
    private String sourceCurrency;

    @Schema(description = "Code ISO 4217 de la devise cible",
            example = "EUR")
    private String targetCurrency;

    @Schema(description = "Montant initial à convertir",
            example = "100.00")
    private Double amount;

    @Schema(description = "Montant converti dans la devise cible",
            example = "92.35")
    private Double convertedAmount;

    @Schema(description = "Taux de change appliqué (1 sourceCurrency = X targetCurrency)",
            example = "0.9235")
    private Double rate;

    @Schema(description = "Horodatage de la conversion",
            example = "2026-09-02T10:30:00")
    private LocalDateTime timestamp;
}
