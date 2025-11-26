package com.hsbc.market.exception;

/**
 * ML API 異常
 * 當與 Python ML API 通訊失敗時拋出
 */
public class MlApiException extends RuntimeException {
    
    public MlApiException(String message) {
        super(message);
    }
    
    public MlApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
