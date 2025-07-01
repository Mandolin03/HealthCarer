package com.evaruiz.healthcarer.security;

import com.evaruiz.healthcarer.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserService userDetailsService) throws Exception {

        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/css/**", "/images/**", "/js/**",
                                            "/register", "/errorPage").permitAll()
                        .anyRequest().authenticated()

                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .failureUrl("/login?error=true")
                        .defaultSuccessUrl("/index", true)
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );
        http.csrf(AbstractHttpConfigurer::disable);
        http.userDetailsService(userDetailsService);
        return http.build();
    }
}
