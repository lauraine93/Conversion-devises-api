package com.Conversion.de.Devises.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse d'erreur standardisée")
public class ErrorResponse {

    @Schema(description = "Horodatage de l'erreur",
            example = "2026-09-02T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Code HTTP de la réponse",
            example = "400")
    private int status;

    @Schema(description = "Type d'erreur HTTP",
            example = "Bad Request")
    private String error;

    @Schema(description = "Message détaillé de l'erreur",
            example = "La devise source ne peut pas être vide")
    private String message;
}
