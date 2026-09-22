package com.Conversion.de.Devises.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de conversion de devises")
public class ConversionRequest {

    @NotBlank(message = "La devise source ne peut pas être vide")
    @Schema(description = "Code ISO 4217 de la devise source (ex: USD, EUR, GBP)",
            example = "USD",
            minLength = 3,
            maxLength = 3)
    private String sourceCurrency;

    @NotBlank(message = "La devise cible ne peut pas être vide")
    @Schema(description = "Code ISO 4217 de la devise cible (ex: USD, EUR, GBP)",
            example = "EUR",
            minLength = 3,
            maxLength = 3)
    private String targetCurrency;

    @NotNull(message = "Le montant ne peut pas être null")
    @Positive(message = "Le montant doit être supérieur à zéro")
    @Schema(description = "Montant à convertir (doit être supérieur à 0)",
            example = "100.00",
            minimum = "0.01")
    private Double amount;
}
