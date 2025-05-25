package rizzerve.menuservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RestTemplate restTemplate;          // will be injected via reflection

    private AuthService authService;

    @BeforeEach
    void setup() {
        authService = new AuthService();
        // Inject mocks / test URL
        ReflectionTestUtils.setField(authService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(authService, "authServiceUrl", "http://auth-service");
    }

    @Test
    void validateToken_okResponse_returnsTrue() {
        ResponseEntity<Map> ok = new ResponseEntity<>(Collections.emptyMap(), HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                                   any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ok);

        assertTrue(authService.validateToken("token"));
    }

    @Test
    void validateToken_unauthorizedException_returnsFalse() {
        HttpClientErrorException unauthorized =
                HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "401", HttpHeaders.EMPTY, null, null);

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenThrow(unauthorized);

        assertFalse(authService.validateToken("token"));
    }

    @Test
    void validateToken_genericException_returnsFalse() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("boom"));

        assertFalse(authService.validateToken("token"));
    }

    @Test
    void getUsernameFromToken_okResponse_returnsEmail() {
        Map<String, Object> body = new HashMap<>();
        body.put("email", "user@example.com");
        ResponseEntity<Map> ok = new ResponseEntity<>(body, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET),
                                   any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ok);

        assertEquals("user@example.com", authService.getUsernameFromToken("token"));
    }

    @Test
    void getUsernameFromToken_okNoEmail_returnsNull() {
        ResponseEntity<Map> ok = new ResponseEntity<>(Collections.emptyMap(), HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenReturn(ok);

        assertNull(authService.getUsernameFromToken("token"));
    }

    @Test
    void getUsernameFromToken_httpError_returnsNull() {
        HttpClientErrorException forbidden =
                HttpClientErrorException.create(HttpStatus.FORBIDDEN, "403", HttpHeaders.EMPTY, null, null);

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenThrow(forbidden);

        assertNull(authService.getUsernameFromToken("token"));
    }

    @Test
    void getUserRoles_okWithRole_returnsArray() {
        Map<String, Object> body = new HashMap<>();
        body.put("role", "ADMIN");
        ResponseEntity<Map> ok = new ResponseEntity<>(body, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenReturn(ok);

        String[] roles = authService.getUserRoles("token");
        assertArrayEquals(new String[]{"ADMIN"}, roles);
    }

    @Test
    void getUserRoles_okNoRole_returnsEmptyArray() {
        ResponseEntity<Map> ok = new ResponseEntity<>(Collections.emptyMap(), HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenReturn(ok);

        assertEquals(0, authService.getUserRoles("token").length);
    }

    @Test
    void getUserRoles_exception_returnsEmptyArray() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("boom"));

        assertEquals(0, authService.getUserRoles("token").length);
    }

    @Test
    void authenticate_okResponse_returnsToken() {
        Map<String, Object> body = new HashMap<>();
        body.put("accessToken", "jwt-token");
        ResponseEntity<Map> ok = new ResponseEntity<>(body, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST),
                                   any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ok);

        assertEquals("jwt-token", authService.authenticate("email", "pass"));
    }

    @Test
    void authenticate_okNoToken_returnsNull() {
        ResponseEntity<Map> ok = new ResponseEntity<>(Collections.emptyMap(), HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenReturn(ok);

        assertNull(authService.authenticate("email", "pass"));
    }

    @Test
    void authenticate_exception_returnsNull() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("boom"));

        assertNull(authService.authenticate("email", "pass"));
    }

    @Test
    void hasRole_userHasRole_returnsTrue() {
        AuthService spyService = spy(authService);
        doReturn(new String[]{"ADMIN", "USER"}).when(spyService).getUserRoles("token");

        assertTrue(spyService.hasRole("token", "ADMIN"));
    }

    @Test
    void hasRole_userMissingRole_returnsFalse() {
        AuthService spyService = spy(authService);
        doReturn(new String[]{"USER"}).when(spyService).getUserRoles("token");

        assertFalse(spyService.hasRole("token", "ADMIN"));
    }
}
