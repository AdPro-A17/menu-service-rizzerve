package rizzerve.menuservice.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class MenuItem {
    private String name;
    private String description;
    private Double price;
    private Boolean available = true;
}
