package com.bank.config;

import com.bank.dto.response.ApiResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDto> handleRuntimeException(RuntimeException ex) {
        
        return ResponseEntity.badRequest().body(new ApiResponseDto(ex.getMessage(), false));
    }
}
