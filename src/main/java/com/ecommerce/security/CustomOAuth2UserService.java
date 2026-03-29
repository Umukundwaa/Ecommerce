package com.ecommerce.security;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerId = String.valueOf(attributes.get("sub") != null
                ? attributes.get("sub")
                : attributes.get("id"));

        String email = (String) attributes.get("email");
        String name  = (String) attributes.getOrDefault("name", email);

        String firstName = name;
        String lastName  = "";
        if (name != null && name.contains(" ")) {
            firstName = name.substring(0, name.indexOf(" "));
            lastName  = name.substring(name.indexOf(" ") + 1);
        }

        final String finalFirstName = firstName;
        final String finalLastName  = lastName;
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    String username = provider + "_" + providerId;
                    return User.builder()
                            .username(username)
                            .email(email != null ? email : username + "@" + provider + ".com")
                            .password("OAUTH2_NO_PASSWORD")   // OAuth users don't have a local password
                            .firstName(finalFirstName)
                            .lastName(finalLastName)
                            .roles(Set.of("ROLE_USER"))
                            .provider(provider)
                            .providerId(providerId)
                            .build();
                });

        user.setFirstName(firstName);
        user.setLastName(lastName);
        if (email != null) user.setEmail(email);
        userRepository.save(user);

        log.info("OAuth2 login: provider={}, email={}", provider, email);

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );
    }
}
