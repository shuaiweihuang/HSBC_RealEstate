package com.hsbc.market.controller;

import com.hsbc.market.client.MlApiClient;
import com.hsbc.market.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康檢查控制器
 * 提供應用和依賴服務的健康狀態
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HealthController {
    
    private final MlApiClient mlApiClient;
    
    /**
     * 應用健康檢查
     * 
     * @return 健康狀態
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkHealth() {
        log.debug("Health check requested");
        
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("application", "HSBC Market Analysis API");
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("version", "1.0.0");
        
        // 檢查 ML API 連接
        try {
            String mlHealth = mlApiClient.checkHealth().block();
            healthStatus.put("ml_api", "CONNECTED");
            healthStatus.put("ml_api_status", mlHealth);
        } catch (Exception e) {
            log.warn("ML API health check failed: {}", e.getMessage());
            healthStatus.put("ml_api", "DISCONNECTED");
            healthStatus.put("ml_api_error", e.getMessage());
        }
        
        return ResponseEntity.ok(
            ApiResponse.success("Service is healthy", healthStatus)
        );
    }
    
    /**
     * 簡單的 ping 端點
     * 
     * @return pong
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
