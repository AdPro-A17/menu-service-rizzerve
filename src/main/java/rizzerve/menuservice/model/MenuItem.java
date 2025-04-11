package rizzerve.menuservice.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public abstract class MenuItem {
    private UUID id;
    private String name;
    private String description;
    private Double price;
    private Boolean available = true;
}
