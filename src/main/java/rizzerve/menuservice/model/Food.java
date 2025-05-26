package rizzerve.menuservice.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("FOOD")
@Getter
@Setter
public class Food extends MenuItem {
    private Boolean isSpicy;
}
