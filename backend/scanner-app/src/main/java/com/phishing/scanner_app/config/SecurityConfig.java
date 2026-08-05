package com.phishing.scanner_app.config;

import com.phishing.scanner_app.auth.OAuth2LoginSuccessHandler;
import com.phishing.scanner_app.auth.MailboxAuthorizationRequestResolver;
import com.phishing.scanner_app.security.JwtAuthFilter;
import com.phishing.scanner_app.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil) {
        return new JwtAuthFilter(jwtUtil);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/public/**", "/actuator/health", "/actuator/info").permitAll()

                .requestMatchers("/api/oauth/**", "/oauth2/**", "/login/oauth2/**", "/health").permitAll()
                
                // Only ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/phishing/reports").hasRole("ADMIN")

                // USER or ADMIN
                .requestMatchers("/user/**").permitAll()
                .requestMatchers("/api/phishing/scan").permitAll()
                .requestMatchers("/api/phishing/history/**").permitAll()
                .requestMatchers("/api/phishing/flags/mine").permitAll()
                .requestMatchers("/api/phishing/reports/*").permitAll()
                .requestMatchers("/api/phishing/reports/*/flags").permitAll()
                .requestMatchers("/api/phishing/reports/*/findings/*/flags").permitAll()
                .requestMatchers("/api/mail/**").permitAll()

                .anyRequest().authenticated()
            )
            // .oauth2Login(...) removed for demo mode
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
