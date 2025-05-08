package rizzerve.menuservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.model.Food;
import rizzerve.menuservice.service.MenuService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@WebMvcTest(MenuController.class)
public class MenuControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MenuService menuService;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    private Food sampleFood;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
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

        Mockito.when(menuService.addMenuItem(Mockito.eq(MenuType.FOOD), any(MenuItemRequest.class)))
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
}
