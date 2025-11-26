package com.hsbc.market.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 導入 Slf4j

import java.io.Serializable;

/**
 * 房產物業實體類
 * 對應 CSV 數據集的所有欄位
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j // 添加 Slf4j
public class Property implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 唯一識別碼
     */
    private Long id;
    
    /**
     * 房屋面積（平方英尺）
     */
    private Integer squareFootage;
    
    /**
     * 臥室數量
     */
    private Integer bedrooms;
    
    /**
     * 浴室數量
     */
    private Double bathrooms;
    
    /**
     * 建造年份
     */
    private Integer yearBuilt;
    
    /**
     * 地塊大小
     */
    private Integer lotSize;
    
    /**
     * 距離市中心距離（公里）
     */
    private Double distanceToCityCenter;
    
    /**
     * 學校評分
     */
    private Double schoolRating;
    
    /**
     * 房價（美元）
     */
    private Double price;
    
    /**
     * 安全地將字符串解析為 Integer，失敗時返回 null
     */
    private static Integer safeParseInt(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse Integer: {}", s);
            return null;
        }
    }

    /**
     * 安全地將字符串解析為 Double，失敗時返回 null
     */
    private static Double safeParseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse Double: {}", s);
            return null;
        }
    }

    /**
     * 從 CSV 行創建 Property 對象
     */
    public static Property fromCsvRow(String[] row) {
        if (row.length < 9) {
            log.error("Invalid CSV row format: row length is less than 9");
            return null; // 返回 null 讓調用者處理
        }
        
        try {
            return Property.builder()
                    // 0. ID - 必須是 Long
                    .id(safeParseInt(row[0]) != null ? safeParseInt(row[0]).longValue() : null) 
                    // 1. Square Footage
                    .squareFootage(safeParseInt(row[1]))
                    // 2. Bedrooms
                    .bedrooms(safeParseInt(row[2]))
                    // 3. Bathrooms
                    .bathrooms(safeParseDouble(row[3]))
                    // 4. Year Built
                    .yearBuilt(safeParseInt(row[4]))
                    // 5. Lot Size
                    .lotSize(safeParseInt(row[5]))
                    // 6. Distance to City Center
                    .distanceToCityCenter(safeParseDouble(row[6]))
                    // 7. School Rating
                    .schoolRating(safeParseDouble(row[7]))
                    // 8. Price - 關鍵字段
                    .price(safeParseDouble(row[8]))
                    .build();
        } catch (Exception e) {
            // 捕獲任何意外的異常 (例如 row 索引越界)
            log.error("Error creating Property from CSV row: {}", String.join(",", row), e);
            return null;
        }
    }
    
    /**
     * 轉換為 CSV 行
     */
    public String[] toCsvRow() {
        return new String[]{
            String.valueOf(id),
            String.valueOf(squareFootage),
            String.valueOf(bedrooms),
            String.valueOf(bathrooms),
            String.valueOf(yearBuilt),
            String.valueOf(lotSize),
            String.valueOf(distanceToCityCenter),
            String.valueOf(schoolRating),
            String.valueOf(price)
        };
    }
}
