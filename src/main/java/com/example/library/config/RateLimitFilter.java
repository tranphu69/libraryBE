package com.example.library.config;

import com.example.library.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {
    private final RedisService redisService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<RateRule> RULES = List.of(
            new RateRule("/api/authentication/login", 5, Duration.ofMinutes(1)),
            new RateRule("/api/authentication/login/verify-otp", 5, Duration.ofMinutes(1)),
            new RateRule("/api/authentication/refresh", 10, Duration.ofMinutes(1)),
            new RateRule("/api/**", 100, Duration.ofMinutes(1))
    );

    private record RateRule(String pattern, int limit, Duration window) {
        boolean isWildcard() {
            return pattern.contains("*");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        RateRule rule = resolveRule(request.getRequestURI());
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }
        String identifier = resolveIdentifier(request);
        String key = "rate_limit:" + sanitize(rule.pattern()) + ":" + identifier;
        long currentCount = redisService.increment(key, rule.window());
        if (currentCount > rule.limit()) {
            log.warn(">>> Rate limit exceeded. key={}, count={}, limit={}", key, currentCount, rule.limit());
            writeTooManyRequests(response, rule.window());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private RateRule resolveRule(String uri) {
        return RULES.stream()
                .filter(r -> pathMatcher.match(r.pattern(), uri))
                .min(Comparator.comparing(RateRule::isWildcard))
                .orElse(null);
    }

    private String resolveIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }
        return "ip:" + resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String sanitize(String pattern) {
        return pattern.replace("/", "_").replace("*", "x");
    }

    private void writeTooManyRequests(HttpServletResponse response, Duration window) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(window.toSeconds()));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
                {"code": 429, "message": "Bạn đã gửi quá nhiều yêu cầu, vui lòng thử lại sau"}
                """);
    }
}
