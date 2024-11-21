package com.java.firebase.demo.Security;

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

    // FirebaseTokenFilter is a custom filter for validating Firebase tokens.
    private final FirebaseTokenFilter firebaseTokenFilter;

    // Constructor for dependency injection of FirebaseTokenFilter.
    public SecurityConfig(FirebaseTokenFilter firebaseTokenFilter) {
        this.firebaseTokenFilter = firebaseTokenFilter;
    }

    /**
     * Configures the security filter chain for the application.
     *
     * @param http the HttpSecurity object for configuring HTTP security.
     * @return the configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable Cross-Origin Resource Sharing (CORS) and CSRF protection.
            .cors(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)

            // Set session management to stateless as the application uses token-based authentication.
            .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Add the FirebaseTokenFilter after the BasicAuthenticationFilter.
            .addFilterAfter((Filter) firebaseTokenFilter, BasicAuthenticationFilter.class)

            // Configure authorization rules for different HTTP requests.
            .authorizeHttpRequests(
                authorizeRequests -> authorizeRequests    

                    // Allow unauthenticated access to user-related routes for account creation and login.
                    .requestMatchers(HttpMethod.POST, "/user", "/login", "/user/resendVerification", "/user/verifyEmail").permitAll()
                    
                    // Require authentication for other user-related routes.
                    .requestMatchers("/user", "/user/**").authenticated()
                    
                    // Allow unauthenticated access to view tournament data.
                    .requestMatchers(HttpMethod.GET, "/tournament/get/all", "/tournament/get/**", "/tournament/round/get/**", "/tournament/round/standing/get/**").permitAll()

                    // Require authentication for updating match results and joining/removing oneself from a tournament.
                    .requestMatchers(HttpMethod.PUT, "/tournament/round/match/update/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/tournament/player/addSelf").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/tournament/player/removeSelf").authenticated()

                    // Require admin role for all other tournament-related routes.
                    .requestMatchers("/tournament/**").hasRole("ADMIN")

                    // Require authentication for uploading images.
                    .requestMatchers(HttpMethod.POST, "/api/image/upload").authenticated()

                    // Restrict all other image-related routes to admin users only.
                    .requestMatchers("/api/image/**").hasRole("ADMIN")
            );
        
        // Build and return the configured SecurityFilterChain.
        return http.build();
    }
}
