package com.medgo.member.config;

import com.medgo.member.service.AzureKeyVaultService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
@DependsOn("azureKeyVaultService") // 👈 ensures Key Vault loads first
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

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
            this.apiKeyHeader = sanitize(keyVaultService.getSecretValue("app-security-api-key-header"));
            this.apiKeySingle = sanitize(keyVaultService.getSecretValue("app-security-api-key"));
            this.basicUser = sanitize(keyVaultService.getSecretValue("app-security-user"));
            this.basicPasswordBcrypt = sanitize(keyVaultService.getSecretValue("app-security-password-bcrypt"));

            log.info("✅ Security secrets successfully loaded from Azure Key Vault.");
        } catch (Exception e) {
            log.error("❌ Failed to initialize SecurityConfig: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize SecurityConfig: " + e.getMessage(), e);
        }
    }

    private String sanitize(String val) {
        return (val == null) ? null : val.replaceAll("[\\t\\n\\r]", "").trim();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder pe) {
        // ensure values are not null
        if (basicUser == null || basicPasswordBcrypt == null) {
            log.error("❌ Missing credentials from Key Vault, cannot initialize UserDetailsService");
            throw new IllegalStateException("Key Vault secrets not initialized for SecurityConfig");
        }
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
