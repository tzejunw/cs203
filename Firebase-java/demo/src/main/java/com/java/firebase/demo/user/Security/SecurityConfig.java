package com.java.firebase.demo.user.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import jakarta.servlet.Filter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FirebaseTokenFilter firebaseTokenFilter;

    public SecurityConfig(FirebaseTokenFilter firebaseTokenFilter) {
        this.firebaseTokenFilter = firebaseTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterAfter((Filter) firebaseTokenFilter, BasicAuthenticationFilter.class)
            .authorizeHttpRequests(
                authorizeRequests -> authorizeRequests
// For easy testing purposes, uncomment this for all tournaments routes to be permitted.
                    // .requestMatchers("/tournament/**").permitAll()    

                    // User Route
                    .requestMatchers(HttpMethod.POST, "/user/create", "/user/login", "/user/masscreate").permitAll()
                    .requestMatchers("/user/**").authenticated()
                    
                    // Tournament Route
                    .requestMatchers("/tournament/get/all", "/tournament/get").permitAll()
                    .requestMatchers("/tournament/**").hasRole("ADMIN")

                    // Image Route
                    .requestMatchers(HttpMethod.POST, "/api/image/upload").authenticated()
                    .requestMatchers("/api/image/**").hasRole("ADMIN") // Only admins can access other image-related routes

            );
        return http.build();
    }
}
