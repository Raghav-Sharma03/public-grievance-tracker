package com.grievance.grievance_tracker.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/images/**", "/error")
                    .permitAll()
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "OFFICER")
                .requestMatchers("/citizen/**").hasRole("CITIZEN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(customSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public org.springframework.security.web.authentication
            .AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();
            String role = authorities.iterator().next().getAuthority();
            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_OFFICER")) {
                response.sendRedirect("/admin/dashboard");
            } else {
                response.sendRedirect("/citizen/dashboard");
            }
        };
    }
}
