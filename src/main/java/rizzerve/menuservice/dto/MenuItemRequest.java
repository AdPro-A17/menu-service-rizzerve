package rizzerve.menuservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuItemRequest {
    
    @NotBlank(message = "Name cannot be empty")
    private String name;
    
    @NotBlank(message = "Description cannot be empty")
    private String description;
    
    @Positive(message = "Price must be positive")
    private Double price;
    
    private Boolean isSpicy;
    
    private Boolean isCold;
    
    private String image;
}