package com.example.library.config;

import com.example.library.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {
    private static final String[] PUBLIC_ENDPOINT_POST = {"/api/authentication/login", "/api/authentication/introspect", "/api/authentication/refresh", "/api/authentication/login/verify-otp"};
    private static final String[] PUBLIC_ENDPOINT_GET = {"/api/redis/ping", "/api/redis/search"};
    private static final String[] AUTHENTICATED_ENDPOINT_GET = {"/api/authentication/profile"};
    private static final String[] AUTHENTICATED_ENDPOINT_POST = {"/api/authentication/logout"};
    @Autowired
    private CustomJwtDecoder customJwtDecoder;
    @Autowired
    private RedisService redisService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RateLimitFilter rateLimitFilter() {return new RateLimitFilter(redisService);}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(request ->
                request.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINT_POST).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_ENDPOINT_GET).permitAll()
                        .requestMatchers(HttpMethod.GET, AUTHENTICATED_ENDPOINT_GET).authenticated()
                        .requestMatchers(HttpMethod.POST, AUTHENTICATED_ENDPOINT_POST).authenticated()
                        .anyRequest().authenticated());
        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        http.csrf(AbstractHttpConfigurer::disable);
        http.addFilterAfter(rateLimitFilter(), BearerTokenAuthenticationFilter.class);
        http.headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives(
                                "default-src 'none'; " +
                                        "frame-ancestors 'none'; " +
                                        "base-uri 'none'"
                        )
                )
                .referrerPolicy(referrer -> referrer
                        .policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
        );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        log.info(">>> CORS allowed origins: {}", config.getAllowedOrigins());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    };
}
