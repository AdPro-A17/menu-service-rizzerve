package rizzerve.menuservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import rizzerve.menuservice.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Minimal implementation for RED phase
        // Will be properly implemented in GREEN phase
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        // This is just for testing purposes
        UserDetails adminUser = User.builder()
            .username("admin")
            .password("{noop}password") // {noop} means no encoding for simplicity in tests
            .roles("ADMIN")
            .build();
        
        return new InMemoryUserDetailsManager(adminUser);
    }
}
