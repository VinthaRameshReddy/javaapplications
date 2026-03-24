package com.medgo.config;

import com.medgo.filter.JwtRequestFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Slf4j(topic = "WebSecurityConfig")
public class JwtWebSecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public JwtWebSecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Initializing BCryptPasswordEncoder with strength 11 in shared JwtWebSecurityConfig");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(11);
        log.info("BCryptPasswordEncoder successfully initialized");
        return encoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security");
//        http.csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(auth -> auth
//                                               .requestMatchers(new AntPathRequestMatcher("/**", HttpMethod.OPTIONS
//                                               .name())).permitAll()
//                                               .requestMatchers(new AntPathRequestMatcher("/register/**")).permitAll()
//                                               .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
//                                               .requestMatchers(new AntPathRequestMatcher("/encrypt")).permitAll()
//                                               .requestMatchers(new AntPathRequestMatcher("/decrypt")).permitAll()
//                                               .requestMatchers(new AntPathRequestMatcher("/api/v1/generateToken"))
//                                               .permitAll()
//                                               .anyRequest().authenticated()
//                )
//                .exceptionHandling(exceptions -> exceptions
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized Munawwar");
//                        })
//                )
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .addFilterAfter(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
//
//        log.info("Security configured with JWT filter");
//        return http.build();

        // For testing purposes, disable security completely : Munawwar
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(AbstractHttpConfigurer::disable)
                .securityMatcher("/**"); // Optional: match all endpoints

        return http.build();
    }

}
