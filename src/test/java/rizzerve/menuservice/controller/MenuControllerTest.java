package rizzerve.menuservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.model.Food;
import rizzerve.menuservice.security.JwtService;
import rizzerve.menuservice.service.MenuService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuService menuService;

    @MockitoBean 
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private Food sampleFood;
    private User adminUser;
    private MenuItemRequest validRequest;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Create a sample food item for testing
        sampleFood = new Food();
        sampleFood.setId(UUID.randomUUID());
        sampleFood.setName("Burger");
        sampleFood.setDescription("Beef burger");
        sampleFood.setPrice(25000.0);
        sampleFood.setIsSpicy(true);
        sampleFood.setAvailable(true);

        // Create admin user for authentication tests
        adminUser = new User(
            "admin",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // Set up JWT token
        jwtToken = "valid.jwt.token";
        
        // Configure mocks
        Mockito.when(jwtService.extractUsername(anyString())).thenReturn("admin");
        Mockito.when(userDetailsService.loadUserByUsername("admin")).thenReturn(adminUser);
        Mockito.when(jwtService.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
        
        // Set up request body
        validRequest = new MenuItemRequest();
        validRequest.setName("Burger");
        validRequest.setDescription("Beef burger");
        validRequest.setPrice(25000.0);
        validRequest.setIsSpicy(true);
    }

    // PUBLIC ACCESS TESTS - No Authentication Required
    
    @Test
    public void testGetAllMenuItems_noAuthRequired() throws Exception {
        Mockito.when(menuService.getAllMenuItems())
                .thenReturn(List.of(sampleFood));

        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Burger"));
    }

    @Test
    public void testGetMenuItemById_noAuthRequired() throws Exception {
        UUID id = sampleFood.getId();
        Mockito.when(menuService.getMenuItemById(id)).thenReturn(sampleFood);

        mockMvc.perform(get("/menu/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Burger"));
    }

    @Test
    public void testGetNonExistentMenuItemById_noAuthRequired() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        Mockito.when(menuService.getMenuItemById(nonExistentId)).thenReturn(null);

        mockMvc.perform(get("/menu/{id}", nonExistentId.toString()))
                .andExpect(status().isNotFound());
    }

    // AUTHENTICATION FAILURE TESTS
    
    @Test
    @WithAnonymousUser
    public void testCreateMenuItem_unauthenticatedShouldFail() throws Exception {
        mockMvc.perform(post("/menu")
                .param("menuType", "FOOD")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());  // Change from isUnauthorized to isForbidden
    }

    @Test
    @WithAnonymousUser
    public void testUpdateMenuItem_unauthenticatedShouldFail() throws Exception {
        UUID id = sampleFood.getId();
        
        mockMvc.perform(put("/menu/{id}", id.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());  // Change from isUnauthorized to isForbidden
    }

    @Test
    @WithAnonymousUser
    public void testDeleteMenuItem_unauthenticatedShouldFail() throws Exception {
        UUID id = sampleFood.getId();

        mockMvc.perform(delete("/menu/{id}", id.toString()))
                .andExpect(status().isForbidden());  // Change from isUnauthorized to isForbidden
    }
    
    // AUTHORIZATION FAILURE TESTS
    
    @Test
    @WithMockUser(roles = "USER")
    public void testCreateMenuItem_authenticatedNonAdminShouldFail() throws Exception {
        mockMvc.perform(post("/menu")
                .param("menuType", "FOOD")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testUpdateMenuItem_authenticatedNonAdminShouldFail() throws Exception {
        UUID id = sampleFood.getId();
        
        mockMvc.perform(put("/menu/{id}", id.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testDeleteMenuItem_authenticatedNonAdminShouldFail() throws Exception {
        UUID id = sampleFood.getId();

        mockMvc.perform(delete("/menu/{id}", id.toString()))
                .andExpect(status().isForbidden());
    }
    
    // ADMIN AUTHENTICATION TESTS - Success cases
    
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testCreateMenuItem_authenticatedAdminShouldSucceed() throws Exception {
        Mockito.when(menuService.addMenuItem(eq(MenuType.FOOD), any(MenuItemRequest.class)))
                .thenReturn(sampleFood);

        mockMvc.perform(post("/menu")
                .param("menuType", "FOOD")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Burger"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testUpdateMenuItem_authenticatedAdminShouldSucceed() throws Exception {
        UUID id = sampleFood.getId();
        
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("Updated Burger");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(30000.0);
        updateRequest.setIsSpicy(false);
        
        Food updatedFood = new Food();
        updatedFood.setId(id);
        updatedFood.setName("Updated Burger");
        updatedFood.setDescription("Updated description");
        updatedFood.setPrice(30000.0);
        updatedFood.setIsSpicy(false);
        updatedFood.setAvailable(true);
        
        Mockito.when(menuService.updateMenuItem(eq(id), any(MenuItemRequest.class)))
                .thenReturn(updatedFood);
        
        mockMvc.perform(put("/menu/{id}", id.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Burger"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.price").value(30000.0))
                .andExpect(jsonPath("$.isSpicy").value(false));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testDeleteMenuItem_authenticatedAdminShouldSucceed() throws Exception {
        UUID id = sampleFood.getId();
        Mockito.when(menuService.deleteMenuItem(id)).thenReturn(sampleFood);

        mockMvc.perform(delete("/menu/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Burger"));
    }
    
    @Test
    public void testCreateMenuItem_withValidJwtToken() throws Exception {
        // Mock service responses
        Mockito.when(menuService.addMenuItem(eq(MenuType.FOOD), any(MenuItemRequest.class)))
                .thenReturn(sampleFood);
        
        // Perform the request with JWT token
        mockMvc.perform(post("/menu")
                .param("menuType", "FOOD")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(SecurityMockMvcRequestPostProcessors.user(adminUser)))
                .andExpect(status().isCreated());
    }
    
    // VALIDATION TESTS

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testUpdateMenuItemWithInvalidData_authenticatedAdmin() throws Exception {
        UUID id = sampleFood.getId();
        
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("");  // Empty name
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(30000.0);
        
        mockMvc.perform(put("/menu/{id}", id.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testUpdateNonExistentMenuItem_authenticatedAdmin() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(15000.0);
        
        Mockito.when(menuService.updateMenuItem(eq(nonExistentId), any(MenuItemRequest.class)))
                .thenReturn(null);
        
        mockMvc.perform(put("/menu/{id}", nonExistentId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testCreateMenuItemWithInvalidMenuType_authenticatedAdmin() throws Exception {
        mockMvc.perform(post("/menu")
                .param("menuType", "INVALID_TYPE")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testCreateMenuItemWithEmptyName_authenticatedAdmin() throws Exception {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("");  // Empty name
        request.setDescription("Test description");
        request.setPrice(15000.0);
        
        mockMvc.perform(post("/menu")
                .param("menuType", "FOOD")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testCreateMenuItemWithEmptyDescription_authenticatedAdmin() throws Exception {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Test Item");
        request.setDescription("");  // Empty description
        request.setPrice(15000.0);
        
        mockMvc.perform(post("/menu")
                .param("menuType", "FOOD")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testCreateMenuItemWithNegativePrice_authenticatedAdmin() throws Exception {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Test Item");
        request.setDescription("Test description");
        request.setPrice(-100.0);  // Negative price
        
        mockMvc.perform(post("/menu")
                .param("menuType", "FOOD")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testDeleteNonExistentMenuItem_authenticatedAdmin() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        Mockito.when(menuService.deleteMenuItem(nonExistentId)).thenReturn(null);

        mockMvc.perform(delete("/menu/{id}", nonExistentId.toString()))
                .andExpect(status().isNotFound());
    }
}
