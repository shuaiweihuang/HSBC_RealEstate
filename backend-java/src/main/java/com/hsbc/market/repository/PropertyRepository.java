package com.hsbc.market.repository;

import com.hsbc.market.model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 物業數據訪問接口
 * 支援多種數據源實作（CSV、Database 等）
 */
public interface PropertyRepository {
    
    /**
     * 查詢所有物業
     */
    List<Property> findAll();
    
    /**
     * 分頁查詢所有物業
     */
    Page<Property> findAll(Pageable pageable);
    
    /**
     * 根據 ID 查詢
     */
    Optional<Property> findById(Long id);
    
    /**
     * 根據臥室數查詢
     */
    List<Property> findByBedrooms(Integer bedrooms);
    
    /**
     * 根據價格範圍查詢
     */
    List<Property> findByPriceRange(Double minPrice, Double maxPrice);
    
    /**
     * 根據建造年份範圍查詢
     */
    List<Property> findByYearRange(Integer yearFrom, Integer yearTo);
    
    /**
     * 複合條件查詢（支援分頁和排序）
     */
    Page<Property> findByFilters(
        Integer bedrooms,
        Double minPrice,
        Double maxPrice,
        Integer yearFrom,
        Integer yearTo,
        Pageable pageable
    );
    
    /**
     * 獲取總數
     */
    long count();
    
    /**
     * 計算平均價格
     */
    Double getAveragePrice();
    
    /**
     * 計算平均面積
     */
    Double getAverageSquareFootage();
    
    /**
     * 獲取最舊建築年份
     */
    Integer getOldestYear();
    
    /**
     * 獲取最新建築年份
     */
    Integer getNewestYear();
}
