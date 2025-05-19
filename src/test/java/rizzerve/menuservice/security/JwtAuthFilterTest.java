package rizzerve.menuservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;
    
    @Mock
    private UserDetailsService userDetailsService;

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Directly instantiate the filter since it's no longer a Spring bean
        jwtAuthFilter = new JwtAuthFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_withValidToken() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        
        UserDetails userDetails = User.withUsername("admin")
                .password("password")
                .roles("ADMIN")
                .build();
        
        when(jwtService.extractUsername(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtService.validateToken(token, userDetails)).thenReturn(true);
        
        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("admin", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(jwtService).extractUsername(token);
        verify(userDetailsService).loadUserByUsername("admin");
        verify(jwtService).validateToken(token, userDetails);
    }

    @Test
    void testDoFilterInternal_withInvalidToken() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = "invalid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        
        UserDetails userDetails = User.withUsername("admin")
                .password("password")
                .roles("ADMIN")
                .build();
        
        when(jwtService.extractUsername(token)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtService.validateToken(token, userDetails)).thenReturn(false);
        
        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_withNoToken() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        
        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractUsername(anyString());
    }
    
    @Test
    void testDoFilterInternal_withNonBearerToken() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        
        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        
        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).extractUsername(anyString());
    }
}
