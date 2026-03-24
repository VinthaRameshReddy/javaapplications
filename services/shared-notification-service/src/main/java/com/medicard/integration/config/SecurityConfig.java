package com.medicard.integration.config;



import com.medicard.integration.service.AzureKeyVaultService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AzureKeyVaultService keyVaultService;

    private boolean apiKeyRequired = true;
    private String apiKeyHeader;
    private String apiKeySingle;
    private String basicUser;
    private String basicPasswordBcrypt;

    public SecurityConfig(AzureKeyVaultService keyVaultService) {
        this.keyVaultService = keyVaultService;
    }

    @PostConstruct
    public void init() {
        try {
            this.apiKeyHeader = keyVaultService.getSecretValue("app-security-api-key-header");
            this.apiKeySingle = keyVaultService.getSecretValue("app-security-api-key");
            this.basicUser = keyVaultService.getSecretValue("app-security-user");
            this.basicPasswordBcrypt = keyVaultService.getSecretValue("app-security-password-bcrypt");

            System.out.println("✅ Security secrets successfully loaded from Azure Key Vault.");
        } catch (Exception e) {
            throw new IllegalStateException("❌ Failed to initialize SecurityConfig: " + e.getMessage(), e);
        }
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder pe) {
        return username -> User.withUsername(basicUser)
                .password(basicPasswordBcrypt)
                .roles("CLIENT")
                .build();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(
                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/health", "/actuator/health").permitAll()
                        .anyRequest().authenticated()
                );

        if (apiKeyRequired) {
            http.addFilterAfter(apiKeyFilter(), BasicAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    OncePerRequestFilter apiKeyFilter() {
        final Set<String> validKeys = (apiKeySingle == null || apiKeySingle.isBlank())
                ? Set.of()
                : Set.of(apiKeySingle.trim());

        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {

                String path = request.getRequestURI();
                if ("/health".equals(path) || "/actuator/health".equals(path)) {
                    chain.doFilter(request, response);
                    return;
                }

                String presented = request.getHeader(apiKeyHeader);

                if (presented == null || presented.isBlank() || (!validKeys.isEmpty() && !validKeys.contains(presented))) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"invalid_or_missing_api_key\"}");
                    return;
                }

                chain.doFilter(request, response);
            }
        };
    }
}




















