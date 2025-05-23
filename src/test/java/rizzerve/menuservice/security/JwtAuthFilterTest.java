package rizzerve.menuservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(jwtService, userDetailsService);
        
        // Clear security context before each test
        SecurityContextHolder.clearContext();
        
        // Create mock user details
        userDetails = new User(
            "admin",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        
        // Default mock responses
        when(request.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
    }

    @Test
    void testDoFilterInternal_withNoAuthHeader() throws ServletException, IOException {
        // Mock no auth header
        when(request.getHeader("Authorization")).thenReturn(null);
        
        // Execute filter
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        
        // Verify filterChain.doFilter was called
        verify(filterChain).doFilter(request, response);
        
        // Verify no interactions with JWT service
        verifyNoInteractions(jwtService);
        
        // Verify no authentication was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_withValidToken() throws ServletException, IOException {
        // Mock JWT service behavior for valid token
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("admin");
        when(jwtService.validateToken(eq("valid.jwt.token"), any(UserDetails.class))).thenReturn(true);
        when(jwtService.extractRoles("valid.jwt.token")).thenReturn(List.of("ROLE_ADMIN"));
        
        // Mock userDetailsService
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        
        // Execute filter
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        
        // Verify methods were called
        verify(userDetailsService).loadUserByUsername("admin");
        verify(jwtService).validateToken(eq("valid.jwt.token"), any(UserDetails.class));
        
        // Verify filterChain.doFilter was called
        verify(filterChain).doFilter(request, response);
        
        // Verify authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void testDoFilterInternal_withInvalidToken() throws ServletException, IOException {
        // Mock JWT service behavior for invalid token
        when(jwtService.extractUsername("valid.jwt.token")).thenReturn("admin");
        when(jwtService.validateToken(eq("valid.jwt.token"), any(UserDetails.class))).thenReturn(false);
        
        // Mock userDetailsService
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        
        // Execute filter
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        
        // Verify methods were called
        verify(userDetailsService).loadUserByUsername("admin");
        verify(jwtService).validateToken(eq("valid.jwt.token"), any(UserDetails.class));
        
        // Verify filterChain.doFilter was called
        verify(filterChain).doFilter(request, response);
        
        // Verify authentication was NOT set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_withException() throws ServletException, IOException {
        // Mock JWT service to throw exception
        when(jwtService.extractUsername("valid.jwt.token")).thenThrow(new RuntimeException("Invalid JWT"));
        
        // Execute filter
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        
        // Verify filterChain.doFilter was called even after exception
        verify(filterChain).doFilter(request, response);
        
        // Verify authentication was NOT set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
