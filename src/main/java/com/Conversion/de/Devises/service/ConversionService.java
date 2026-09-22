package com.Conversion.de.Devises.service;

import com.Conversion.de.Devises.config.CacheConfig;
import com.Conversion.de.Devises.exception.ExternalApiException;
import com.Conversion.de.Devises.exception.InvalidCurrencyException;
import com.Conversion.de.Devises.model.ConversionRequest;
import com.Conversion.de.Devises.model.ConversionResponse;
import com.Conversion.de.Devises.model.ExchangeRateApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@Tag(name = "ConversionService", description = "Service de conversion de devises")
public class ConversionService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
            "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "INR", "MXN",
            "BRL", "KRW", "SGD", "HKD", "NOK", "SEK", "DKK", "PLN", "CZK", "HUF",
            "TRY", "ZAR", "RUB", "BGN", "HRK", "RON", "UAH", "EGP", "AED", "SAR",
            "QAR", "KWD", "BHD", "OMR", "JOD", "LBP", "PKR", "BDT", "LKR", "NPR",
            "THB", "IDR", "MYR", "PHP", "VND", "TWD", "NZD", "FJD", "PGK", "ISK",
            "ALL", "DZD", "MAD", "TND", "NGN", "KES", "GHS", "ETB", "TZS",
            "XOF", "XAF", "CDF", "MGA", "UGW", "BWP", "SZL", "LSL", "NAD"
    );

    private final WebClient webClient;

    @Value("${exchange.api.url}")
    private String apiUrl;

    @Value("${exchange.api.key}")
    private String apiKey;

    public ConversionService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Convertit un montant d'une devise à une autre en utilisant les taux de change cachés.
     *
     * @param request Requête contenant la devise source, la devise cible et le montant
     * @return ConversionResponse avec le montant converti et le taux appliqué
     * @throws InvalidCurrencyException si une des devises n'est pas supportée
     * @throws ExternalApiException si l'appel à l'API externe échoue
     */
    public ConversionResponse convert(ConversionRequest request) {
        String source = request.getSourceCurrency().toUpperCase();
        String target = request.getTargetCurrency().toUpperCase();

        // Validation des devises
        validateCurrency(source, "source");
        validateCurrency(target, "cible");

        // Récupération des taux de change (avec cache)
        Map<String, Double> rates = getExchangeRates(source);

        // Vérification que la devise cible est disponible dans les taux
        if (!rates.containsKey(target)) {
            throw new InvalidCurrencyException(
                    "La devise cible '" + target + "' n'est pas disponible pour la conversion depuis " + source);
        }

        Double rate = rates.get(target);
        Double convertedAmount = request.getAmount() * rate;

        log.info("Conversion: {} {} -> {} {} (taux: {})",
                request.getAmount(), source, convertedAmount, target, rate);

        return new ConversionResponse(
                source,
                target,
                request.getAmount(),
                Math.round(convertedAmount * 100.0) / 100.0,
                rate,
                LocalDateTime.now()
        );
    }

    /**
     * Récupère les taux de change depuis l'API externe avec cache (5 minutes).
     *
     * @param baseCurrency Devise de base pour les taux
     * @return Map des taux de change par rapport à la devise de base
     */
    @Cacheable(value = CacheConfig.EXCHANGE_RATE_CACHE, key = "#baseCurrency")
    public Map<String, Double> getExchangeRates(String baseCurrency) {
        String url = apiUrl + "/" + apiKey + "/latest/" + baseCurrency;
        log.info("Appel API externe pour les taux de change: base={}", baseCurrency);

        try {
            ExchangeRateApiResponse response = webClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(ExchangeRateApiResponse.class)
                    .block();

            if (response == null || !"success".equals(response.getResult())) {
                throw new ExternalApiException(
                        "L'API externe a retourné une réponse invalide pour la devise: " + baseCurrency);
            }

            if (response.getConversionRates() == null || response.getConversionRates().isEmpty()) {
                throw new ExternalApiException(
                        "Aucun taux de change disponible pour la devise: " + baseCurrency);
            }

            return response.getConversionRates();
        } catch (WebClientException e) {
            log.error("Erreur de connexion à l'API externe: {}", e.getMessage());
            throw new ExternalApiException(
                    "Impossible de se connecter au service de taux de change. Veuillez réessayer ultérieurement.", e);
        }
    }

    /**
     * Valide qu'une devise est supportée.
     */
    private void validateCurrency(String currency, String fieldName) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new InvalidCurrencyException("La devise " + fieldName + " ne peut pas être vide");
        }
        if (currency.length() != 3) {
            throw new InvalidCurrencyException(
                    "Le code de devise " + fieldName + " doit contenir exactement 3 caractères (ex: USD, EUR)");
        }
        if (!SUPPORTED_CURRENCIES.contains(currency)) {
            throw new InvalidCurrencyException(
                    "La devise " + fieldName + " '" + currency + "' n'est pas supportée. " +
                    "Codes supportés: USD, EUR, GBP, JPY, etc.");
        }
    }
}
