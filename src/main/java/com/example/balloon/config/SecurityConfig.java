//package com.example.balloon.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import java.util.Map;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint((req, res, e) -> writeJsonError(res, 401, "unauthorized"))
//                        .accessDeniedHandler((req, res, e) -> writeJsonError(res, 403, "forbidden"))
//                );
//
//        return http.build();
//    }
//
//    private void writeJsonError(jakarta.servlet.http.HttpServletResponse res, int status, String message) throws java.io.IOException {
//        res.setStatus(status);
//        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        res.getWriter().write(objectMapper.writeValueAsString(Map.of("error", message)));
//    }
//}