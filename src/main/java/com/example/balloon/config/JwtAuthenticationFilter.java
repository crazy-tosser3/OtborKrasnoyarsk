//package com.example.balloon.config;
//
//import com.example.balloon.service.JwtService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//    private final JwtService jwtService;
//    private final UserDetailsService userDetailsService;
//
//    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
//        this.jwtService = jwtService;
//        this.userDetailsService = userDetailsService;
//    }
//

//

//ВОТ Я ОБНОВИЛ
//@Override
//protected void doFilterInternal(
//        HttpServletRequest request,
//        HttpServletResponse response,
//        FilterChain filterChain) throws ServletException, IOException {
//
//    String token = extractTokenFromRequest(request);
//
//    if (token != null && jwtService.validateToken(token)) {
//        try {
//            String username = jwtService.getUsernameFromToken(token);
//            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//
//            if (userDetails.isEnabled()) {
//                UsernamePasswordAuthenticationToken authentication =
//                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }
//        } catch (UsernameNotFoundException e) {
//            SecurityContextHolder.clearContext();
//        }
//    }
//    filterChain.doFilter(request, response);
//}




//    private String extractTokenFromRequest(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }
//}
