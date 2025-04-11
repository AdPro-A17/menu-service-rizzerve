package rizzerve.menuservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuItemRequest {
    private String name;
    private String description;
    private Double price;
    private Boolean isSpicy;
    private Boolean isCold;
}
