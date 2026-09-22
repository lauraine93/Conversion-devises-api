package com.Conversion.de.Devises.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ExchangeRateApiResponse {

    @JsonProperty("result")
    private String result;

    @JsonProperty("base_code")
    private String baseCode;

    @JsonProperty("conversion_rates")
    private Map<String, Double> conversionRates;

    @JsonProperty("time_last_update_unix")
    private Long timeLastUpdateUnix;
}
