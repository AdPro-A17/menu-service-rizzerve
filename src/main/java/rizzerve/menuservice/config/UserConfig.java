package rizzerve.menuservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class UserConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // This is just a placeholder for the JWT authentication flow
            return User.builder()
                    .username(username)
                    .password("")
                    .roles("ADMIN")
                    .build();
        };
    }
}
