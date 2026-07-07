package com.example.library.aspect;

import com.example.library.security.ActorContextProvider;
import com.example.library.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final ActorContextProvider actorContextProvider;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String actorId = actorContextProvider.getActorId();
        String actorRole = actorContextProvider.getActorRole();
        String actorPermissions = actorContextProvider.getActorPermissionsSnapshot();
        String requestJson = serializeRequest(signature.getParameterNames(), args);
        String targetId = resolveTargetId(auditable.targetId(), signature.getParameterNames(), args);
        Object result;
        String status = "SUCCESS";
        String responseJson;
        try {
            result = joinPoint.proceed();
            responseJson = serializeResponse(result);
        } catch (Throwable ex) {
            status = "FAILED";
            responseJson = serializeError(ex);
            safeSaveLog(actorId, actorRole, actorPermissions, auditable, targetId, requestJson, responseJson, status);
            throw ex;
        }
        safeSaveLog(actorId, actorRole, actorPermissions, auditable, targetId, requestJson, responseJson, status);
        return result;
    }

    private void safeSaveLog(String actorId, String actorRole, String actorPermissions, Auditable auditable,
                             String targetId, String requestJson, String responseJson, String status) {
        try {
            auditLogService.save(actorId, actorRole, actorPermissions, auditable.action(),
                    auditable.targetType(), targetId, requestJson, responseJson, status
            );
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    private String serializeRequest(String[] paramNames, Object[] args) {
        try {
            java.util.Map<String, Object> requestMap = new java.util.LinkedHashMap<>();
            for (int i = 0; i < args.length; i++) {
                requestMap.put(paramNames[i], toLoggableValue(args[i]));
            }
            return objectMapper.writeValueAsString(requestMap);
        } catch (Exception e) {
            return "{\"error\":\"cannot serialize request\"}";
        }
    }

    private String serializeResponse(Object result) {
        try {
            return objectMapper.writeValueAsString(toLoggableValue(result));
        } catch (Exception e) {
            return "{\"error\":\"cannot serialize response\"}";
        }
    }

    private Object toLoggableValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof MultipartFile file) {
            java.util.Map<String, Object> meta = new java.util.LinkedHashMap<>();
            meta.put("fileName", file.getOriginalFilename());
            meta.put("contentType", file.getContentType());
            meta.put("size", file.getSize());
            return meta;
        }
        if (value instanceof byte[] bytes) {
            java.util.Map<String, Object> meta = new java.util.LinkedHashMap<>();
            meta.put("type", "binary");
            meta.put("sizeBytes", bytes.length);
            return meta;
        }
        if (value instanceof MultipartFile[] files) {
            return java.util.Arrays.stream(files)
                    .map(this::toLoggableValue)
                    .toList();
        }
        return value;
    }

    private String serializeError(Throwable ex) {
        try {
            java.util.Map<String, String> errorMap = new java.util.LinkedHashMap<>();
            errorMap.put("exception", ex.getClass().getName());
            errorMap.put("message", ex.getMessage());
            return objectMapper.writeValueAsString(errorMap);
        } catch (Exception e) {
            return "{\"error\":\"cannot serialize exception\"}";
        }
    }

    private String resolveTargetId(String expr, String[] paramNames, Object[] args) {
        if (expr == null || expr.isBlank()) {
            return null;
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            for (int i = 0; i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            Expression expression = parser.parseExpression(expr);
            Object value = expression.getValue(context);
            if (value == null) {
                return "UNKNOWN";
            }
            String text = value.toString();
            return text == null || text.isBlank() ? "UNKNOWN" : text;
        } catch (Exception e) {
            log.warn("Cannot resolve targetId expression: {}", expr, e);
            return null;
        }
    }
}
