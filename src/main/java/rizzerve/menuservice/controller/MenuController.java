package rizzerve.menuservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.model.MenuItem;
import rizzerve.menuservice.service.MenuService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    public ResponseEntity<MenuItem> createMenuItem(
            @RequestParam MenuType menuType,
            @RequestBody MenuItemRequest request
    ) {
        MenuItem item = menuService.addMenuItem(menuType, request);
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllMenuItems() {
        List<MenuItem> items = menuService.getAllMenuItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable UUID id) {
        MenuItem item = menuService.getMenuItemById(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MenuItem> deleteMenuItem(@PathVariable UUID id) {
        MenuItem item = menuService.deleteMenuItem(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }
}
