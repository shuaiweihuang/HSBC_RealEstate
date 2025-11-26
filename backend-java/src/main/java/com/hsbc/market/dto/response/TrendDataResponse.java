package com.hsbc.market.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趨勢數據點響應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataResponse {
    
    /**
     * 標籤（例如：年份）
     */
    private String label;
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 平均價格
     */
    private Double avgPrice;
    
    /**
     * 該時間段的物業數量
     */
    private Long count;
}
