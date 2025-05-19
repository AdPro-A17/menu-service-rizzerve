package rizzerve.menuservice.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JwtService {

    // Minimal implementation for the RED phase
    // This will be properly implemented in the GREEN phase

    public String extractUsername(String token) {
        // Not implemented yet - for RED phase testing
        return null;
    }

    public List<String> extractRoles(String token) {
        // Not implemented yet - for RED phase testing
        return new ArrayList<>();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        // Not implemented yet - for RED phase testing
        return false;
    }
}
