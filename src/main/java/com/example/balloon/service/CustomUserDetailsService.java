//package com.example.balloon.service;
//
//import com.example.balloon.model.entity.UserEntity;
//import com.example.balloon.repository.UserRepository;
//import org.springframework.security.authentication.DisabledException;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.*;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//    private final UserRepository userRepository;
//
//    public CustomUserDetailsService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        UserEntity userEntity = userRepository.findByUsername(username);
//        if (userEntity == null) {
//            throw new UsernameNotFoundException("User not found: " + username);
//        }
//        if (!userEntity.isEnabled()) {
//            throw new DisabledException("User is disabled: " + username);
//        }
//
//        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
//                "ROLE_" + userEntity.getRole().getName().name()
//        );
//
//        return User.builder()
//                .username(userEntity.getUsername())
//                .password(userEntity.getPassword())
//                .authorities(Collections.singletonList(authority))
//                .accountExpired(false)
//                .accountLocked(false)
//                .credentialsExpired(false)
//                .disabled(!userEntity.isEnabled())
//                .build();
//    }
//}
