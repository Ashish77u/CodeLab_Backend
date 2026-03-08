package com.codelab.backend.security;


import com.codelab.backend.entity.Role;
import com.codelab.backend.entity.User;
import com.codelab.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
                new DefaultOAuth2UserService();
        OAuth2User oauthUser = delegate.loadUser(request);

        String provider   = request.getClientRegistration().getRegistrationId(); // "google"
        String providerId = oauthUser.getAttribute("sub");   // Google's user ID
        String email      = oauthUser.getAttribute("email");
        String name       = oauthUser.getAttribute("name");
        String picture    = oauthUser.getAttribute("picture");

        // Find existing user or create new one
        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    // Also check by email (user may have registered normally before)
                    return userRepository.findByEmail(email)
                            .map(existing -> {
                                // Link OAuth2 to existing account
                                existing.setProvider(provider);
                                existing.setProviderId(providerId);
                                return userRepository.save(existing);
                            })
                            .orElseGet(() -> {
                                // Brand new user via OAuth2
                                User newUser = User.builder()
                                        .email(email)
                                        .username(generateUsername(name))
                                        .password(null)        // no password for OAuth2 users
                                        .realName(name)
                                        .profileImageUrl(picture)
                                        .role(Role.USER)
                                        .provider(provider)
                                        .providerId(providerId)
                                        .enabled(true)
                                        .build();
                                return userRepository.save(newUser);
                            });
                });

        return oauthUser; // Spring needs this returned; we handle JWT in handler
    }

    private String generateUsername(String name) {
        // e.g. "John Doe" → "johndoe_a3f2"
        String base = name.toLowerCase().replaceAll("\s+", "");
        return base + "_" + UUID.randomUUID().toString().substring(0, 4);
    }
}
