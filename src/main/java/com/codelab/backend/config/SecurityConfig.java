package com.codelab.backend.config;


import com.codelab.backend.security.JwtAuthFilter;
import com.codelab.backend.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
//    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",           // register, login, refresh
            "/v3/api-docs/**",           // Swagger JSON
            "/swagger-ui/**",            // Swagger UI
            "/swagger-ui.html",
            "/oauth2/**",                // OAuth2 redirects
            "/login/oauth2/**",
            "/uploads/**"                // ← ADD THIS

    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — we're stateless (JWT), no session cookie
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS (delegates to CorsConfig bean)
                .cors(cors -> cors.configure(http))

                // No sessions — REST is stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Route-level authorization rules
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/v1/me**").permitAll()
//                        .requestMatchers(PUBLIC_URLS).permitAll()
//                        // Public GET on projects (landing page, project details)
//                        .requestMatchers(
//                                org.springframework.http.HttpMethod.GET,
//                                "/api/v1/projects/**",
//                                "/api/v1/users/*/profile"
//                        ).permitAll()
//                        // Admin-only
//                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
//                        // Everything else requires login
//                        .anyRequest().authenticated()
//                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/me**",
                                "/api/v1/projects",
                                "/api/v1/projects/search",
                                "/api/v1/projects/user/**",
                                "/api/v1/projects/*"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // OAuth2 login configuration
//                .oauth2Login(oauth -> oauth
//                        .successHandler(oAuth2SuccessHandler)
//                )

                // Use our DaoAuthenticationProvider
                .authenticationProvider(authenticationProvider)

                // Add JWT filter BEFORE Spring's default auth filter
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}