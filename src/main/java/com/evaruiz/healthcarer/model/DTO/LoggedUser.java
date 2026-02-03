package com.evaruiz.healthcarer.model.DTO;

import com.evaruiz.healthcarer.model.UserDB;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class LoggedUser implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String password;
    private String role;
    private String name;

    public LoggedUser(UserDB user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getEncodedPassword();
        this.role = user.getRole();
        this.name = user.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
