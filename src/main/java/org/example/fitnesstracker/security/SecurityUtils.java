package org.example.fitnesstracker.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            log.error("User not authenticated or invalid principal type");
            throw new InsufficientAuthenticationException("User not authenticated");
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }

}

