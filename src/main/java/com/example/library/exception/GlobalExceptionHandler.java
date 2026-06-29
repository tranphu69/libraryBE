package com.example.library.exception;

import com.example.library.dto.response.ApiResponse;
import com.example.library.util.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(ErrorCode.UNAUTHENTICATED_FORBIDDEN.getStatusCode())
                .body(ResponseUtils.error(ErrorCode.UNAUTHENTICATED_FORBIDDEN.getCode(), ErrorCode.UNAUTHENTICATED_FORBIDDEN.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(ErrorCode.UNAUTHENTICATED_UNAUTHORIZED.getStatusCode())
                .body(ResponseUtils.error(ErrorCode.UNAUTHENTICATED_UNAUTHORIZED.getCode(), ErrorCode.UNAUTHENTICATED_UNAUTHORIZED.getMessage()));
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
//        return ResponseEntity
//                .status(ErrorCode.ERROR_SYSTEM.getStatusCode())
//                .body(ResponseUtils.error(ErrorCode.ERROR_SYSTEM.getCode(), ErrorCode.ERROR_SYSTEM.getMessage()));
//    }
}
