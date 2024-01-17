package com.hysteryale.model;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="\"user\"")
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Size(min = 2, message = "User name must be at least 2 characters")
    @Column(name = "user_name")
    private String name;

    @NotBlank(message = "Email must not be blank")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;

    @Column(name = "default_locale")
    private String defaultLocale;

    @Column(name = "is_active")
    private boolean isActive;

    @Temporal(TemporalType.DATE)
    @Column(name = "last_login")
    private Date lastLogin;

    public User(String userName, String email, String password, Role role) {
        this.name = userName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(Integer id, String userName, String email, String password, Role role, String defaultLocale, boolean isActive) {
        this.id = id;
        this.name = userName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.defaultLocale = defaultLocale;
        this.isActive = isActive;
    }
    public User(Integer id, String password) {
        this.id = id;
        this.password = password;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String userName, String email, String password, Role role, boolean isActive) {
        this.name = userName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getRoleName()));
    }

    @Override
    public String getUsername() {
        return email;
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
        return true;
    }



}
