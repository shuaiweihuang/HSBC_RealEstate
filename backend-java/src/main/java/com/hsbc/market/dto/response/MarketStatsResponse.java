package com.hsbc.market.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 市場統計響應
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketStatsResponse {
    private Double averagePrice;
    private Double medianPrice;
    private Long totalVolume;
    private Double priceChangePercent;
    private Double averageSquareFootage;
    private Integer oldestYear;
    private Integer newestYear;
}
