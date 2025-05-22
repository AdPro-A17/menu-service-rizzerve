package rizzerve.menuservice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to communicate with the Auth microservice.
 */
@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${auth.service.url:http://localhost:8080}")
    private String authServiceUrl;

    /**
     * Validates a JWT token with the auth service.
     * Makes an API call to the auth service to validate the token.
     * 
     * @param token The JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            String validateTokenUrl = authServiceUrl + "/api/auth/validate";
            
            // Create headers with the token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            // Create the request entity
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // Make API call to validate token
            ResponseEntity<Map> response = restTemplate.exchange(
                validateTokenUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );
            
            // Check if token is valid based on response
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || 
                e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.warn("Token validation failed: {}", e.getMessage());
                return false;
            }
            logger.error("Error validating token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Exception during token validation: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets user details from the auth service.
     * Makes an API call to the auth service to get user information.
     * 
     * @param token The JWT token of the user
     * @return The username of the authenticated user, or null if token is invalid
     */
    public String getUsernameFromToken(String token) {
        try {
            String userInfoUrl = authServiceUrl + "/api/auth/profile";
            
            // Create headers with the token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            // Create the request entity
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // Make API call to get user details
            ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );
            
            // Extract username/email from response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("email");
            }
            
            return null;
            
        } catch (HttpClientErrorException e) {
            logger.warn("Error getting user details: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Exception getting user details: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user roles from the auth service.
     * 
     * @param token The JWT token
     * @return Array of user roles or empty array if token is invalid
     */
    public String[] getUserRoles(String token) {
        try {
            String userInfoUrl = authServiceUrl + "/api/auth/profile";
            
            // Create headers with the token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            // Create the request entity
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            
            // Make API call to get user details
            ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
            );
            
            // Extract roles from response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String roleStr = (String) response.getBody().get("role");
                if (roleStr != null) {
                    return new String[]{roleStr};
                }
            }
            
            return new String[0];
            
        } catch (Exception e) {
            logger.error("Exception getting user roles: {}", e.getMessage());
            return new String[0];
        }
    }
    
    /**
     * Authenticate a user with the auth service.
     * 
     * @param email The user email
     * @param password The user password
     * @return A JWT token if authentication is successful, null otherwise
     */
    public String authenticate(String email, String password) {
        try {
            String loginUrl = authServiceUrl + "/api/auth/login";
            
            // Create login request body
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("email", email);
            loginRequest.put("password", password);
            
            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create the request entity
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest, headers);
            
            // Make API call to login
            ResponseEntity<Map> response = restTemplate.exchange(
                loginUrl,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            // Extract access token from response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("accessToken");
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("Exception during authentication: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if a user has a specific role.
     * 
     * @param token The JWT token
     * @param role The role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(String token, String role) {
        String[] roles = getUserRoles(token);
        for (String userRole : roles) {
            if (userRole.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
