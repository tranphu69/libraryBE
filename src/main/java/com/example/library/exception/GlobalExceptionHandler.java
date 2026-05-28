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
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseUtils.error("403", "Bạn không có quyền truy cập tài nguyên này"));
//        .body(ResponseUtils.error(
//                ErrorCode.ACCESS_DENIED.getCode(),
//                ErrorCode.ACCESS_DENIED.getMessage()
//        ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ResponseUtils.error("401", "Bạn chưa đăng nhập hoặc token không hợp lệ"));
//        .body(ResponseUtils.error(
//                ErrorCode.UNAUTHORIZED.getCode(),
//                ErrorCode.UNAUTHORIZED.getMessage()
//        ));
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ResponseUtils.error("500", "Lỗi hệ thống, vui lòng thử lại sau"));
////        .body(ResponseUtils.error(
////                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
////                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
////        ));
//    }
}
