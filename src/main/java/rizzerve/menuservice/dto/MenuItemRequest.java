package rizzerve.menuservice.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
public class MenuItemRequest {
    @NotBlank(message = "Name cannot be empty")
    private String name;
    
    @NotBlank(message = "Description cannot be empty")
    private String description;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
    
    private Boolean isSpicy;
    private Boolean isCold;
}