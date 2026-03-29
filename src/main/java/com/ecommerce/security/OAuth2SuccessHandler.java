package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String providerId = String.valueOf(
                oAuth2User.getAttribute("sub") != null
                        ? oAuth2User.getAttribute("sub")
                        : oAuth2User.getAttribute("id"));

        User user = userRepository.findByProviderAndProviderId("google", providerId)
                .or(() -> userRepository.findByProviderAndProviderId("github", providerId))
                .orElseThrow(() -> new RuntimeException("OAuth2 user not found in DB"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password("")
                .authorities(user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList())
                .build();

        String token = jwtUtil.generateToken(userDetails);

        log.info("OAuth2 success — issued JWT for user '{}'", user.getUsername());

        // Return JWT in JSON body
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("tokenType", "Bearer");
        body.put("username", user.getUsername());
        body.put("email", user.getEmail());
        body.put("message", "OAuth2 login successful");

        objectMapper.writeValue(response.getWriter(), body);
    }
}
