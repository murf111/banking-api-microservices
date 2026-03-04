package com.portfolio.bank.shared.security;

import com.portfolio.bank.shared.exceptions.NotFoundException;
import com.portfolio.bank.user.api.LoginAttemptService;
import com.portfolio.bank.user.internal.UserEntity;
import com.portfolio.bank.user.internal.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String email) throws NotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                                        .orElseThrow(() -> new NotFoundException("User not found: " + email));

        // 2. Check if blocked by Brute Force (Using email instead of entity)
        boolean isBlockedByBruteForce = loginAttemptService.isBlocked(user.getEmail());

        // FIX: Check the new isLocked field
        boolean isBlockedByAdmin = user.isLocked();
        boolean accountNonLocked = !isBlockedByAdmin && !isBlockedByBruteForce;

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                true,
                true,
                true,
                accountNonLocked,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
