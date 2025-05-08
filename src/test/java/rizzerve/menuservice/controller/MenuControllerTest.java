package rizzerve.menuservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.model.Food;
import rizzerve.menuservice.service.MenuService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
public class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    @Autowired
    private ObjectMapper objectMapper;

    private Food sampleFood;

    @BeforeEach
    void setUp() {
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
}
