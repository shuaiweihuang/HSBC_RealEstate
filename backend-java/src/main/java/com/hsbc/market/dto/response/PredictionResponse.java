package com.hsbc.market.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 價格預測響應
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    
    /**
     * 預測的價格
     */
    @JsonProperty("predicted_price")
    private Double predictedPrice;
    
    /**
     * 預測的置信區間下限
     */
    @JsonProperty("confidence_lower")
    private Double confidenceLower;
    
    /**
     * 預測的置信區間上限
     */
    @JsonProperty("confidence_upper")
    private Double confidenceUpper;
    
    /**
     * 模型版本
     */
    @JsonProperty("model_version")
    private String modelVersion;
}
