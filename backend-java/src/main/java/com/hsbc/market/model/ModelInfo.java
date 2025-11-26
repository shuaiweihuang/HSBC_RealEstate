package com.hsbc.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * ML 模型資訊
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelInfo implements Serializable {
    
    @JsonProperty("model_name")
    private String modelName;
    
    @JsonProperty("model_version")
    private String modelVersion;
    
    @JsonProperty("trained_at")
    private String trainedAt;
    
    @JsonProperty("n_features")
    private Integer nFeatures;
    
    @JsonProperty("feature_names")
    private String[] featureNames;
    
    @JsonProperty("performance")
    private PerformanceMetrics performance;
    
    @JsonProperty("coefficients")
    private Map<String, Double> coefficients;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics implements Serializable {
        @JsonProperty("r2_score")
        private Double r2Score;
        
        @JsonProperty("mse")
        private Double mse;
        
        @JsonProperty("rmse")
        private Double rmse;
        
        @JsonProperty("mae")
        private Double mae;
    }
}
