package com.rs.subscription.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class CustomUserDetails implements UserDetails {

    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String status;

    @JsonIgnore
    private String password;

    private Set<String> authorities;

    public CustomUserDetails(Long userId, String username, String email, String fullName,
                             String status, String password, Set<String> authorities) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.status = status;
        this.password = password;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(status);
    }
}


