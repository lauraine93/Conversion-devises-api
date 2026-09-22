package com.Conversion.de.Devises.controller;

import com.Conversion.de.Devises.exception.ExternalApiException;
import com.Conversion.de.Devises.exception.InvalidCurrencyException;
import com.Conversion.de.Devises.model.ConversionRequest;
import com.Conversion.de.Devises.model.ConversionResponse;
import com.Conversion.de.Devises.model.ErrorResponse;
import com.Conversion.de.Devises.service.ConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Conversion de Devises", description = "API de conversion de devises utilisant des taux de change en temps réel")
public class ConversionController {

    private final ConversionService conversionService;

    /**
     * Convertit un montant d'une devise à une autre.
     *
     * @param request Requête contenant la devise source, la devise cible et le montant
     * @return ConversionResponse avec le montant converti, le taux appliqué et l'horodatage
     */
    @PostMapping("/convert")
    @Operation(
            summary = "Convertir une devise",
            description = "Convertit un montant d'une devise source vers une devise cible en utilisant les taux de change " +
                          "en temps réel. Les taux de change sont mis en cache pendant 5 minutes pour optimiser les performances. " +
                          "Les devises doivent être codées selon la norme ISO 4217 (3 lettres majuscules).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objet contenant la devise source, la devise cible et le montant à convertir",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ConversionRequest.class),
                            mediaType = "application/json"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ Conversion réussie. Le montant converti et le taux appliqué sont retournés.",
                    content = @Content(
                            schema = @Schema(implementation = ConversionResponse.class),
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Exemple de conversion USD -> EUR",
                                    value = """
                                            {
                                              "sourceCurrency": "USD",
                                              "targetCurrency": "EUR",
                                              "amount": 100.00,
                                              "convertedAmount": 92.35,
                                              "rate": 0.9235,
                                              "timestamp": "2026-09-02T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ Requête invalide. Le corps de la requête contient des données manquantes, " +
                                  "un montant négatif ou nul, ou des codes de devise invalides (moins de 3 caractères).",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = "application/json",
                            examples = {
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Champ manquant",
                                            value = """
                                                    {
                                                      "timestamp": "2026-09-02T10:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "sourceCurrency: La devise source ne peut pas être vide"
                                                    }
                                                    """
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Montant invalide",
                                            value = """
                                                    {
                                                      "timestamp": "2026-09-02T10:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "amount: Le montant doit être supérieur à zéro"
                                                    }
                                                    """
                                    ),
                                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "Devise non supportée",
                                            value = """
                                                    {
                                                      "timestamp": "2026-09-02T10:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "La devise source 'XYZ' n'est pas supportée. Codes supportés: USD, EUR, GBP, JPY, etc."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "⚠️ Erreur de la passerelle (Bad Gateway). Le service de taux de change externe " +
                                  "est indisponible ou a retourné une réponse invalide. Cela peut survenir en cas de " +
                                  "panne de l'API ExchangeRate-API ou de clé API invalide.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "API externe indisponible",
                                    value = """
                                            {
                                              "timestamp": "2026-09-02T10:30:00",
                                              "status": 502,
                                              "error": "Bad Gateway",
                                              "message": "Impossible de se connecter au service de taux de change. Veuillez réessayer ultérieurement."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "💥 Erreur interne du serveur. Une erreur inattendue s'est produite dans l'application. " +
                                  "Veuillez contacter l'administrateur si le problème persiste.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Erreur interne",
                                    value = """
                                            {
                                              "timestamp": "2026-09-02T10:30:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Une erreur interne est survenue: ..."
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ConversionResponse> convert(@Valid @org.springframework.web.bind.annotation.RequestBody ConversionRequest request) {
        log.info("Demande de conversion: {} {} -> {}",
                request.getAmount(), request.getSourceCurrency(),
                request.getTargetCurrency());

        ConversionResponse response = conversionService.convert(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
