package rizzerve.menuservice.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("DRINK")
@Getter
@Setter
public class Drink extends MenuItem {
    private Boolean isCold;
}
