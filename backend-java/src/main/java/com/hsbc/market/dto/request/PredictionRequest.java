package com.hsbc.market.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
/**
 * 價格預測請求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {
    
    /**
     * 房屋面積（平方英尺）
     */
    @NotNull(message = "Square footage is required")
    @Min(value = 500, message = "Square footage must be at least 500")
    @Max(value = 10000, message = "Square footage must not exceed 10000")
    @JsonProperty("square_footage")
    private Integer squareFootage;
    
    /**
     * 臥室數量
     */
    @NotNull(message = "Number of bedrooms is required")
    @Min(value = 1, message = "Must have at least 1 bedroom")
    @Max(value = 10, message = "Bedrooms must not exceed 10")
    private Integer bedrooms;
    
    /**
     * 浴室數量
     */
    @NotNull(message = "Number of bathrooms is required")
    @Min(value = 1, message = "Must have at least 1 bathroom")
    @Max(value = 10, message = "Bathrooms must not exceed 10")
    private Double bathrooms;
    
    /**
     * 建造年份
     */
    @NotNull(message = "Year built is required")
    @Min(value = 1900, message = "Year built must be after 1900")
    @Max(value = 2030, message = "Year built must not exceed 2030")
    @JsonProperty("year_built")
    private Integer yearBuilt;
    
    /**
     * 地塊大小
     */
    @NotNull(message = "Lot size is required")
    @Min(value = 1000, message = "Lot size must be at least 1000")
    @Max(value = 50000, message = "Lot size must not exceed 50000")
    @JsonProperty("lot_size")
    private Integer lotSize;
    
    /**
     * 距離市中心評分
     */
    @NotNull(message = "Distance to city center is required")
    @Min(value = 0, message = "City center rating cannot be negative")
    @Max(value = 10, message = "City center rating must not exceed 10")
    @JsonProperty("distance_to_city_center")
    private Double distanceToCityCenter;
    
    /**
     * 學校評分
     */
    @NotNull(message = "School rating is required")
    @Min(value = 1, message = "School rating must be at least 1")
    @Max(value = 10, message = "School rating must not exceed 10")
    @JsonProperty("school_rating")
    private Double schoolRating;
}
