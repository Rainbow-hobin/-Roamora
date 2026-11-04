package com.zhituxing.security;

import com.zhituxing.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String name;
    private final String role;
    private final String status;
    private final String password;

    public UserPrincipal(Long id, String name, String role, String status, String password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.status = status;
        this.password = password;
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(user.getId(), user.getName(), user.getRole(), user.getStatus(), user.getPassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return "正常".equals(status);
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
}
