package rizzerve.menuservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import rizzerve.menuservice.security.JwtAuthFilter;
import rizzerve.menuservice.security.JwtService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // CSRF protection is intentionally disabled for this stateless REST API.
            // This is safe because:
            // 1. Authentication uses JWT tokens in Authorization headers, not cookies
            // 2. Stateless session management prevents session fixation attacks
            // 3. CSRF attacks target cookie-based authentication, which we don't use
            // 4. All requests require explicit Authorization header with valid JWT
            .csrf(AbstractHttpConfigurer::disable) // NOSONAR - Justified above
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - read operations
                .requestMatchers("GET", "/menu", "/menu/**").permitAll()
                // All other endpoints require authentication and proper role
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    
    @Bean
    public JwtAuthFilter jwtAuthenticationFilter() {
        return new JwtAuthFilter(jwtService, userDetailsService);
    }
}
