package com.example.sptrngboot.config;

import com.example.sptrngboot.model.AdminUser;
import com.example.sptrngboot.repository.AdminUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/styles.css", "/scripts.js", "/", "/login", "/chatbot").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/chatbot/history").permitAll()
                .requestMatchers(HttpMethod.POST, "/store/*/reviews", "/store/*/reserve", "/api/chatbot/message", "/api/ballet/consultations").permitAll()
                .requestMatchers("/admin/**").authenticated()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                new AntPathRequestMatcher("/admin/**"),
                new AntPathRequestMatcher("/api/chatbot/message", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/api/ballet/consultations", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/store/**"),
                new AntPathRequestMatcher("/store/*/reviews", HttpMethod.POST.name()),
                new AntPathRequestMatcher("/store/*/reserve", HttpMethod.POST.name())
            ))
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin", true)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(AdminUserRepository adminUserRepository) {
        return username -> {
            AdminUser user = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            String[] roles = user.getRoles() == null || user.getRoles().isBlank()
                ? new String[]{"ADMIN"}
                : java.util.Arrays.stream(user.getRoles().split(","))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .toArray(String[]::new);

            return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles)
                .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
