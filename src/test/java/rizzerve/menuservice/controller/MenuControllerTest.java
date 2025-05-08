package rizzerve.menuservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.exception.GlobalExceptionHandler;
import rizzerve.menuservice.model.Food;
import rizzerve.menuservice.service.MenuService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@EnableWebMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {
    MenuController.class,
    GlobalExceptionHandler.class,
    ValidationAutoConfiguration.class,
    MenuControllerTest.TestConfig.class
})
public class MenuControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public MenuService menuService() {
            return Mockito.mock(MenuService.class);
        }
        
        @Bean
        public LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MenuService menuService;

    @Autowired
    private ObjectMapper objectMapper;

    private Food sampleFood;

    @BeforeEach
    void setUp() {
        // Reset mock before each test
        Mockito.reset(menuService);
        
        sampleFood = new Food();
        sampleFood.setId(UUID.randomUUID());
        sampleFood.setName("Burger");
        sampleFood.setDescription("Beef burger");
        sampleFood.setPrice(25000.0);
        sampleFood.setIsSpicy(true);
        sampleFood.setAvailable(true);
    }

    @Test
    void testCreateMenuItem() throws Exception {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Burger");
        request.setDescription("Beef burger");
        request.setPrice(25000.0);
        request.setIsSpicy(true);

        Mockito.when(menuService.addMenuItem(eq(MenuType.FOOD), any(MenuItemRequest.class)))
                .thenReturn(sampleFood);

        mockMvc.perform(post("/menu")
                        .param("menuType", "FOOD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Burger"));
    }


    @Test
    void testGetAllMenuItems() throws Exception {
        Mockito.when(menuService.getAllMenuItems())
                .thenReturn(List.of(sampleFood));

        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetMenuItemById() throws Exception {
        UUID id = sampleFood.getId();
        Mockito.when(menuService.getMenuItemById(id)).thenReturn(sampleFood);

        mockMvc.perform(get("/menu/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Burger"));
    }

    @Test
    void testDeleteMenuItem() throws Exception {
        UUID id = sampleFood.getId();
        Mockito.when(menuService.deleteMenuItem(id)).thenReturn(sampleFood);

        mockMvc.perform(delete("/menu/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Burger"));
    }

    @Test
    void testUpdateMenuItem() throws Exception {
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
        
        Mockito.when(menuService.updateMenuItem(Mockito.eq(id), any(MenuItemRequest.class)))
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
    void testUpdateNonExistentMenuItem() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(15000.0);
        
        Mockito.when(menuService.updateMenuItem(Mockito.eq(nonExistentId), any(MenuItemRequest.class)))
                .thenReturn(null);
        
        mockMvc.perform(put("/menu/{id}", nonExistentId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateMenuItemWithInvalidData() throws Exception {
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
    void testGetNonExistentMenuItemById() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        Mockito.when(menuService.getMenuItemById(nonExistentId)).thenReturn(null);

        mockMvc.perform(get("/menu/{id}", nonExistentId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteNonExistentMenuItem() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        Mockito.when(menuService.deleteMenuItem(nonExistentId)).thenReturn(null);

        mockMvc.perform(delete("/menu/{id}", nonExistentId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateMenuItemWithInvalidMenuType() throws Exception {
        MenuItemRequest request = new MenuItemRequest();
        request.setName("Burger");
        request.setDescription("Beef burger");
        request.setPrice(25000.0);
        
        mockMvc.perform(post("/menu")
                .param("menuType", "INVALID_TYPE")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateMenuItemWithEmptyName() throws Exception {
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
    void testCreateMenuItemWithEmptyDescription() throws Exception {
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
    void testCreateMenuItemWithNegativePrice() throws Exception {
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
}
