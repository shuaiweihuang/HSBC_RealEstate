package com.hsbc.market.service;

import com.hsbc.market.dto.response.MarketStatsResponse;
import com.hsbc.market.dto.response.TrendDataResponse;
import com.hsbc.market.exception.DataNotFoundException;
import com.hsbc.market.model.Property;
import com.hsbc.market.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 市場分析服務
 * 提供市場統計、趨勢分析等功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarketAnalysisService {
    
    private final PropertyRepository propertyRepository;
    
    /**
     * 獲取市場整體統計
     */
    @Cacheable(value = "marketStats", key = "'overall'")
    public MarketStatsResponse getMarketStatistics() {
        log.info("Calculating market statistics");
        
        List<Property> properties = propertyRepository.findAll();
        
        // 檢查數據是否載入
        if (properties.isEmpty()) {
            throw new DataNotFoundException("No property data available for statistics calculation.");
        }
        
        // 過濾掉價格無效或為 null 的物業，確保計算的準確性
        List<Property> validProperties = properties.stream()
                .filter(p -> p.getPrice() != null && !p.getPrice().isNaN() && p.getPrice() > 0)
                .collect(Collectors.toList());

        if (validProperties.isEmpty()) {
            throw new DataNotFoundException("No valid property data available after filtering.");
        }
        
        // 計算統計數據
        double avgPrice = validProperties.stream()
                .mapToDouble(Property::getPrice)
                .average()
                .orElse(0.0);
        
        double medianPrice = calculateMedian(validProperties.stream()
                .mapToDouble(Property::getPrice)
                .sorted()
                .toArray());
        
        long totalVolume = validProperties.size();
        
        double avgSquareFootage = validProperties.stream()
                .filter(p -> p.getSquareFootage() != null)
                .mapToDouble(Property::getSquareFootage)
                .average()
                .orElse(0.0);
        
        Integer oldestYear = validProperties.stream()
                .filter(p -> p.getYearBuilt() != null)
                .map(Property::getYearBuilt)
                .min(Integer::compare)
                .orElse(null);

        Integer newestYear = validProperties.stream()
                .filter(p -> p.getYearBuilt() != null)
                .map(Property::getYearBuilt)
                .max(Integer::compare)
                .orElse(null);
        
        // 計算價格變化百分比
        double priceChangePercent = calculatePriceChangePercent(validProperties, oldestYear, newestYear);
        
        return MarketStatsResponse.builder()
                .averagePrice(avgPrice)
                .medianPrice(medianPrice)
                .totalVolume(totalVolume)
                .priceChangePercent(priceChangePercent)
                .averageSquareFootage(avgSquareFootage)
                .oldestYear(oldestYear)
                .newestYear(newestYear)
                .build();
    }
    
    /**
     * 獲取市場趨勢數據 (按年份平均價格)
     */
    @Cacheable(value = "marketTrend", key = "'byYear'")
    public List<TrendDataResponse> getMarketTrend() {
        log.info("Calculating market trend by year");
        
        List<Property> properties = propertyRepository.findAll();
        
        if (properties.isEmpty()) {
            throw new DataNotFoundException("No property data available for trend calculation.");
        }
        
        try {
            // **關鍵修復點: 過濾掉 yearBuilt 為 null 的物業，避免 Collectors.groupingBy 拋出 NullPointerException**
            List<Property> validTrendProperties = properties.stream()
                    .filter(p -> p.getYearBuilt() != null && p.getPrice() != null && p.getPrice() > 0)
                    .collect(Collectors.toList());
            
            if (validTrendProperties.isEmpty()) {
                throw new DataNotFoundException("No valid property data with YearBuilt and Price available for trend calculation.");
            }

            // 1. 按 yearBuilt 分組
            Map<Integer, List<Property>> propertiesByYear = validTrendProperties.stream()
                    .collect(Collectors.groupingBy(Property::getYearBuilt));

            // 2. 計算每個年份的趨勢數據
            return propertiesByYear.entrySet().stream()
                    .map(entry -> {
                        Integer year = entry.getKey();
                        List<Property> yearProperties = entry.getValue();

                        // 計算平均價格
                        double avgPrice = yearProperties.stream()
                                .mapToDouble(Property::getPrice)
                                .average()
                                .orElse(0.0);

                        // 構建響應
                        return TrendDataResponse.builder()
                                .year(year)
                                .label(String.valueOf(year))
                                .avgPrice(avgPrice)
                                .count((long) yearProperties.size())
                                .build();
                    })
                    .sorted(Comparator.comparing(TrendDataResponse::getYear))
                    .collect(Collectors.toList());
        } catch (DataNotFoundException e) {
            // 重新拋出 DataNotFoundException
            throw e;
        } catch (Exception e) {
            log.error("Error calculating market trend, possibly due to bad data: {}", e.getMessage(), e);
            // 重新拋出為運行時異常，讓 GlobalExceptionHandler 處理 500
            throw new IllegalStateException("Market trend calculation failed: Invalid data detected in the group. Please check logs.", e);
        }
    }
    
    /**
     * 獲取按臥室數量分組的統計數據
     */
    @Cacheable(value = "marketSegments", key = "'byBedrooms'")
    public Map<Integer, MarketStatsResponse> getStatsByBedrooms() {
        log.info("Calculating market segments by bedrooms");
        
        List<Property> properties = propertyRepository.findAll();
        
        if (properties.isEmpty()) {
            throw new DataNotFoundException("No property data available for segmentation calculation.");
        }
        
        try {
            // 過濾掉 bedrooms 為 null 的物業
            List<Property> validSegmentProperties = properties.stream()
                    .filter(p -> p.getBedrooms() != null)
                    .collect(Collectors.toList());

            if (validSegmentProperties.isEmpty()) {
                 throw new DataNotFoundException("No valid property data with Bedrooms available for segment calculation.");
            }

            return validSegmentProperties.stream()
                    .collect(Collectors.groupingBy(Property::getBedrooms))
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> calculateStatsForGroup(entry.getValue())
                    ));
        } catch (DataNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calculating market segments by bedrooms: {}", e.getMessage(), e);
            throw new IllegalStateException("Market segment calculation failed: Invalid data detected.", e);
        }
    }

    /**
     * 計算中位數
     */
    private double calculateMedian(double[] data) {
        if (data == null || data.length == 0) return 0.0;
        int n = data.length;
        if (n % 2 == 1) {
            return data[n / 2];
        } else {
            return (data[n / 2 - 1] + data[n / 2]) / 2.0;
        }
    }

    /**
     * 計算價格變化百分比
     */
    private double calculatePriceChangePercent(List<Property> properties, Integer oldestYear, Integer newestYear) {
        if (oldestYear == null || newestYear == null || oldestYear.equals(newestYear)) return 0.0;
        
        // 確保 oldestYear 和 newestYear 在列表的 YearBuilt 中找到
        if (properties.stream().filter(p -> p.getYearBuilt() != null).map(Property::getYearBuilt).noneMatch(oldestYear::equals)) return 0.0;
        if (properties.stream().filter(p -> p.getYearBuilt() != null).map(Property::getYearBuilt).noneMatch(newestYear::equals)) return 0.0;
        
        double oldAvg = properties.stream()
                .filter(p -> p.getYearBuilt() != null && p.getYearBuilt().equals(oldestYear))
                .mapToDouble(Property::getPrice)
                .average()
                .orElse(0.0);
        
        double newAvg = properties.stream()
                .filter(p -> p.getYearBuilt() != null && p.getYearBuilt().equals(newestYear))
                .mapToDouble(Property::getPrice)
                .average()
                .orElse(0.0);
        
        if (oldAvg == 0) return 0.0;
        return ((newAvg - oldAvg) / oldAvg) * 100;
    }
    
    /**
     * 統計特定年份的物業數量 (未在當前方法中使用)
     */
    private long countPropertiesByYear(List<Property> properties, Integer year) {
        return properties.stream()
                .filter(p -> p.getYearBuilt() != null && p.getYearBuilt().equals(year))
                .count();
    }
    
    /**
     * 計算特定組的統計數據
     */
    private MarketStatsResponse calculateStatsForGroup(List<Property> properties) {
        // 過濾掉價格無效或為 null 的物業
        List<Property> validProperties = properties.stream()
                .filter(p -> p.getPrice() != null && !p.getPrice().isNaN() && p.getPrice() > 0)
                .collect(Collectors.toList());

        if (validProperties.isEmpty()) {
            // 如果組內沒有有效數據，返回零值統計
            return MarketStatsResponse.builder().totalVolume(0L).build();
        }
        
        double avgPrice = validProperties.stream()
                .mapToDouble(Property::getPrice)
                .average()
                .orElse(0.0);
        
        double medianPrice = calculateMedian(validProperties.stream()
                .mapToDouble(Property::getPrice)
                .sorted()
                .toArray());
        
        // 計算其他統計數據
        double avgSquareFootage = validProperties.stream()
                .filter(p -> p.getSquareFootage() != null)
                .mapToDouble(Property::getSquareFootage)
                .average()
                .orElse(0.0);
        
        Integer oldestYear = validProperties.stream()
                .filter(p -> p.getYearBuilt() != null)
                .map(Property::getYearBuilt)
                .min(Integer::compare)
                .orElse(null);

        Integer newestYear = validProperties.stream()
                .filter(p -> p.getYearBuilt() != null)
                .map(Property::getYearBuilt)
                .max(Integer::compare)
                .orElse(null);
        
        double priceChangePercent = calculatePriceChangePercent(validProperties, oldestYear, newestYear);
        
        return MarketStatsResponse.builder()
                .averagePrice(avgPrice)
                .medianPrice(medianPrice)
                .totalVolume((long) validProperties.size())
                .priceChangePercent(priceChangePercent)
                .averageSquareFootage(avgSquareFootage)
                .oldestYear(oldestYear)
                .newestYear(newestYear)
                .build();
    }
}
