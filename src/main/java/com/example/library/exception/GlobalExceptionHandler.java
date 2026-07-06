package com.example.library.exception;

import com.example.library.dto.response.ApiResponse;
import com.example.library.util.ResponseUtils;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.text.ParseException;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResponseUtils.error(errorCode.getCode(), ex.getFormattedMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseUtils.error(errorCode.getCode(), ex.getFormattedMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSize(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(ErrorCode.LIMIT_FILE.getStatusCode())
                .body(ResponseUtils.error(ErrorCode.LIMIT_FILE.getCode(), ErrorCode.LIMIT_FILE.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(ErrorCode.UNAUTHORIZED.getStatusCode())
                .body(ResponseUtils.error(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(ErrorCode.UNAUTHENTICATED.getStatusCode())
                .body(ResponseUtils.error(ErrorCode.UNAUTHENTICATED.getCode(), ErrorCode.UNAUTHENTICATED.getMessage()));
    }

    @ExceptionHandler({ParseException.class, JOSEException.class})
    public ResponseEntity<ApiResponse<Void>> handleJwtException(Exception ex) {
        return ResponseEntity
                .status(ErrorCode.AUTHENTICATION_TOKEN.getStatusCode())
                .body(ResponseUtils.error(ErrorCode.AUTHENTICATION_TOKEN.getCode(), ErrorCode.AUTHENTICATION_TOKEN.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(ErrorCode.ERROR_SYSTEM.getStatusCode())
                .body(ResponseUtils.error(ErrorCode.ERROR_SYSTEM.getCode(), ErrorCode.ERROR_SYSTEM.getMessage()));
    }
}
