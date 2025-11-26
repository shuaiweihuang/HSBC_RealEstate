package com.hsbc.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * HSBC Market Analysis API - Main Application
 * 
 * 提供房地產市場分析和價格預測服務
 * 
 * @author HSBC Team
 * @version 1.0
 */
@SpringBootApplication
@EnableCaching
public class MarketAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketAnalysisApplication.class, args);
    }
}
