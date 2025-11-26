package com.hsbc.market.controller;

import com.hsbc.market.dto.request.PredictionRequest;
import com.hsbc.market.dto.response.ApiResponse;
import com.hsbc.market.dto.response.PredictionResponse;
import com.hsbc.market.model.Property;
import com.hsbc.market.repository.PropertyRepository;
import com.hsbc.market.service.PredictionService; // <-- 新增導入
import jakarta.validation.Valid; // <-- 新增導入
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono; // <-- 新增導入

/**
 * 物業管理控制器
 * 提供物業查詢、過濾、分頁等功能
 */
@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PropertyController {
    
    private final PropertyRepository propertyRepository;
    private final PredictionService predictionService; // <-- 注入 PredictionService
    
    /**
     * 分頁查詢所有物業
     * * @param page 頁碼（從0開始）
     * @param size 每頁大小
     * @param sortBy 排序欄位（price, squareFootage, yearBuilt等）
     * @param direction 排序方向（asc, desc）
     * @return 分頁物業列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Property>>> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        log.info("Received request for all properties - page: {}, size: {}, sort: {} {}", 
                 page, size, sortBy, direction);

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Property> properties = propertyRepository.findAll(pageable);
        
        return ResponseEntity.ok(
            ApiResponse.success("Properties retrieved successfully", properties)
        );
    }

    /**
     * 根據過濾條件查詢物業
     * * @param bedrooms 臥室數量
     * @param minPrice 價格下限
     * @param maxPrice 價格上限
     * @param yearFrom 建造年份起始
     * @param yearTo 建造年份結束
     * @param page 頁碼
     * @param size 每頁大小
     * @return 過濾後的物業列表
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<Property>>> getFilteredProperties(
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Received filter request: bedrooms={}, minPrice={}, maxPrice={}, yearFrom={}, yearTo={}", 
                 bedrooms, minPrice, maxPrice, yearFrom, yearTo);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Property> filteredProperties = propertyRepository.findByFilters(
            bedrooms, minPrice, maxPrice, yearFrom, yearTo, pageable
        );
        
        return ResponseEntity.ok(
            ApiResponse.success("Filtered properties retrieved successfully", filteredProperties)
        );
    }
    
    /**
     * What-if 分析（預測）
     * * @param request 預測請求參數
     * @return 預測結果
     */
    @PostMapping("/what-if")
    public Mono<ResponseEntity<ApiResponse<PredictionResponse>>> whatIfAnalysis(
            @Valid @RequestBody PredictionRequest request // <-- 使用 PredictionRequest DTO 並進行驗證
    ) {
        log.info("Received what-if analysis request for: {}", request);
        
        // 呼叫 PredictionService 獲取預測結果，並將 Mono<PredictionResponse> 轉換為 Mono<ResponseEntity<...>>
        return predictionService.getPricePrediction(request)
                .map(predictionResponse -> 
                    ResponseEntity.ok(
                        ApiResponse.success("Property price predicted successfully", predictionResponse)
                    )
                );
    }
}
