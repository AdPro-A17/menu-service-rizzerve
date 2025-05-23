package rizzerve.menuservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rizzerve.menuservice.dto.MenuItemRequest;
import rizzerve.menuservice.enums.MenuType;
import rizzerve.menuservice.model.MenuItem;
import rizzerve.menuservice.service.MenuService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItem> createMenuItem(
            @RequestParam MenuType menuType,
            @Valid @RequestBody MenuItemRequest request
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItem> deleteMenuItem(@PathVariable UUID id) {
        MenuItem item = menuService.deleteMenuItem(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItem> updateMenuItem(
            @PathVariable UUID id,
            @Valid @RequestBody MenuItemRequest request
    ) {
        MenuItem item = menuService.updateMenuItem(id, request);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    /* Asynchronous API endpoints */

    @GetMapping("/async")
    public CompletableFuture<ResponseEntity<List<MenuItem>>> getAllMenuItemsAsync() {
        return menuService.getAllMenuItemsAsync()
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/async/{id}")
    public CompletableFuture<ResponseEntity<MenuItem>> getMenuItemByIdAsync(@PathVariable UUID id) {
        return menuService.getMenuItemByIdAsync(id)
                .thenApply(item -> item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build());
    }

    @PostMapping("/async")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<MenuItem>> createMenuItemAsync(
            @RequestParam MenuType menuType,
            @Valid @RequestBody MenuItemRequest request
    ) {
        return menuService.addMenuItemAsync(menuType, request)
                .thenApply(item -> new ResponseEntity<>(item, HttpStatus.CREATED));
    }

    @PutMapping("/async/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<MenuItem>> updateMenuItemAsync(
            @PathVariable UUID id,
            @Valid @RequestBody MenuItemRequest request
    ) {
        return menuService.updateMenuItemAsync(id, request)
                .thenApply(item -> item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build());
    }

    @DeleteMapping("/async/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<MenuItem>> deleteMenuItemAsync(@PathVariable UUID id) {
        return menuService.deleteMenuItemAsync(id)
                .thenApply(item -> item != null ? ResponseEntity.ok(item) : ResponseEntity.notFound().build());
    }
}
