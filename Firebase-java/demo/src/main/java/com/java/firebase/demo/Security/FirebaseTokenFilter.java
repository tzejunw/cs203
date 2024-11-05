package com.java.firebase.demo.Security;

import java.io.IOException;
import java.util.Collections; 

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Extract token from the request header
        String token = getTokenFromRequest(request);

        if (token != null) {
            try {
                FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token, true);
                authenticateBasedOnRole(firebaseToken);
            } catch (FirebaseAuthException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // Continue with the next filter in the chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the token from the Authorization header of the request.
     * @param request the HTTP request containing the Authorization header
     * @return the token if present, or null if not found
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        return (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) 
            ? bearerToken.substring(BEARER_PREFIX.length()) : null;
    }

    /**
     * Sets the authentication based on the user's role in Firebase custom claims.
     * @param firebaseToken the decoded Firebase token containing user claims
     */
    private void authenticateBasedOnRole(FirebaseToken firebaseToken) {
        boolean isAdmin = Boolean.TRUE.equals(firebaseToken.getClaims().get("admin"));

        // Set the appropriate authentication based on role
        PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
                isAdmin ? firebaseToken : firebaseToken.getUid(), 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority(isAdmin ? "ROLE_ADMIN" : "ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}