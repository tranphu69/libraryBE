package com.example.library.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ActorContextProvider {
    private final ObjectMapper objectMapper;

    public String getActorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return "SYSTEM";
        }
        return auth.getName();
    }

    public String getActorRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "[]";
        }
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(authority -> authority.startsWith("ROLE_"))
                .distinct()
                .sorted()
                .toList();
        try {
            return objectMapper.writeValueAsString(roles);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    public String getActorPermissionsSnapshot() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "[]";
        }
        List<String> permissions = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .distinct()
                .sorted()
                .toList();
        try {
            return objectMapper.writeValueAsString(permissions);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
