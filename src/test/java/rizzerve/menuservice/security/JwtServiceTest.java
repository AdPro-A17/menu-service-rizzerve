package rizzerve.menuservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", SECRET_KEY);
    }

    @Test
    void testExtractUsername() {
        String username = "admin";
        String token = generateToken(username);
        
        String extractedUsername = jwtService.extractUsername(token);
        
        assertEquals(username, extractedUsername);
    }

    @Test
    void testExtractRoles() {
        String username = "admin";
        Map<String, Object> claims = new HashMap<>();
        // Change from "roles" to "role" to match the JwtService implementation
        claims.put("role", "ADMIN");
        String token = generateTokenWithClaims(username, claims);
        
        List<String> roles = jwtService.extractRoles(token);
        
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("ROLE_ADMIN", roles.get(0));
    }

    @Test
    void testValidateToken_withValidToken() {
        String username = "admin";
        String token = generateToken(username);
        UserDetails userDetails = User.withUsername(username)
            .password("password")
            .roles("ADMIN")
            .build();
        
        boolean isValid = jwtService.validateToken(token, userDetails);
        
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_withInvalidUsername() {
        String token = generateToken("admin");
        UserDetails userDetails = User.withUsername("wronguser")
            .password("password")
            .roles("ADMIN")
            .build();
        
        boolean isValid = jwtService.validateToken(token, userDetails);
        
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_withExpiredToken() {
        String token = generateExpiredToken("admin");
        UserDetails userDetails = User.withUsername("admin")
            .password("password")
            .roles("ADMIN")
            .build();
        
        boolean isValid = jwtService.validateToken(token, userDetails);
        
        assertFalse(isValid);
    }

    @Test
    void testExtractAllClaims() {
        String username = "admin";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "value");
        String token = generateTokenWithClaims(username, extraClaims);
        
        Claims claims = jwtService.extractAllClaims(token);
        
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals("value", claims.get("customClaim"));
    }

    private String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSignInKey())
                .compact();
    }

    private String generateTokenWithClaims(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSignInKey())
                .compact();
    }

    private String generateExpiredToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60))
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60))
                .signWith(getSignInKey())
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
