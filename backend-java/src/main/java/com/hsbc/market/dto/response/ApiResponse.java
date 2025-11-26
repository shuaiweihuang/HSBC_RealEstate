package com.hsbc.market.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 統一的 API 響應格式
 * 
 * @param <T> 響應數據類型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /**
     * 響應狀態碼
     */
    private int status;
    
    /**
     * 響應消息
     */
    private String message;
    
    /**
     * 響應數據
     */
    private T data;
    
    /**
     * 時間戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * 錯誤詳情（僅在失敗時）
     */
    private String error;
    
    /**
     * 成功響應
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }
    
    /**
     * 成功響應（帶自訂消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 失敗響應
     */
    public static <T> ApiResponse<T> error(int status, String message, String error) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .error(error)
                .build();
    }
}
