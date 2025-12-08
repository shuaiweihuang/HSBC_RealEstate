package com.hsbc.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 對應 Python ML API /model-info 端點的回傳結構
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelInfo {

    private String target;

    @JsonProperty("features_used")
    private List<String> featuresUsed;

    @JsonProperty("n_samples")
    private Integer nSamples;

    @JsonProperty("train_samples")
    private Integer trainSamples;

    @JsonProperty("test_samples")
    private Integer testSamples;

    @JsonProperty("train_mae")
    private Double trainMae;

    @JsonProperty("test_mae")
    private Double testMae;

    @JsonProperty("train_r2")
    private Double trainR2;

    @JsonProperty("test_r2")
    private Double testR2;

    private Map<String, Double> coefficients;

    @JsonProperty("top_features")
    private List<TopFeature> topFeatures;

    /**
     * 單一特徵影響力
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopFeature {
        private String feature;
        
        @JsonProperty("impact")
        private Double impact;
    }
}
