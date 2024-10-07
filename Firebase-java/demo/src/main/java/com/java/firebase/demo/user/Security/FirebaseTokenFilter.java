package com.java.firebase.demo.user.Security;

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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (token != null) {
            try {
                boolean checkRevoked = true;
                FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token, checkRevoked);

                // Check for the "admin" role in custom claims
                // Extract the custom claims and check if the user has the "admin" role
                boolean isAdmin = firebaseToken.getClaims().containsKey("admin")
                        && (boolean) firebaseToken.getClaims().get("admin");

                if (isAdmin) {
                    // Set authentication and proceed
                    PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
                            firebaseToken, null, 
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Admin authority");
                } else {
                    PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
                            firebaseToken.getUid(), null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Player authority");
                }

            } catch (FirebaseAuthException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}