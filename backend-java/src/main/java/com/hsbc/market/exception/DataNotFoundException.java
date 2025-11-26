package com.hsbc.market.exception;

/**
 * 數據未找到異常
 */
public class DataNotFoundException extends RuntimeException {
    
    public DataNotFoundException(String message) {
        super(message);
    }
    
    public DataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
