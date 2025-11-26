package com.hsbc.market.controller;

import com.hsbc.market.dto.response.ApiResponse;
import com.hsbc.market.dto.response.MarketStatsResponse; // <-- 新增導入
import com.hsbc.market.dto.response.TrendDataResponse;
import com.hsbc.market.service.MarketAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 市場分析控制器
 */
@RestController
@RequestMapping("/api/market-analysis")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MarketAnalysisController {
    
    private final MarketAnalysisService marketAnalysisService;
    
    /**
     * 獲取市場統計數據
     * * @return 市場統計數據
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<MarketStatsResponse>> getMarketStatistics() {
        log.info("Received request for market statistics");
        
        // 臨時使用 MarketStatsResponse 實例化數據
        MarketStatsResponse stats = MarketStatsResponse.builder()
            .averagePrice(260000.0)
            .medianPrice(250000.0)
            .totalVolume(50L)
            .priceChangePercent(2.5)
            .averageSquareFootage(1800.0)
            .oldestYear(1950)
            .newestYear(2022)
            .build();
        
        return ResponseEntity.ok(
            ApiResponse.success("Market statistics retrieved successfully", stats)
        );
    }
    
    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<List<TrendDataResponse>>> getMarketTrend() {
        log.info("Received request for market trend");
        
        List<TrendDataResponse> trend = marketAnalysisService.getMarketTrend();
        
        return ResponseEntity.ok(
            ApiResponse.success("Market trend data retrieved successfully", trend)
        );
    }
    
    @GetMapping("/segments/bedrooms")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatsByBedrooms() {
        log.info("Received request for bedroom segments");
        
        Map<String, Object> segments = Map.of(
            "2_bedrooms", Map.of("count", 15, "avgPrice", 185000),
            "3_bedrooms", Map.of("count", 25, "avgPrice", 255000),
            "4_bedrooms", Map.of("count", 10, "avgPrice", 375000)
        );
        
        return ResponseEntity.ok(
            ApiResponse.success("Bedroom segments retrieved successfully", segments)
        );
    }
}
