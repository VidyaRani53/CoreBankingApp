package com.banksphere.security;

import com.banksphere.entity.User;
import com.banksphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean locked = "LOCKED".equalsIgnoreCase(user.getStatus());
        boolean disabled = "DISABLED".equalsIgnoreCase(user.getStatus());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(
                        user.getRoles().stream()
                                .map(r -> new SimpleGrantedAuthority(r.getName()))
                                .collect(Collectors.toSet())
                )
                .accountLocked(locked)
                .disabled(disabled)
                .build();

    }
}