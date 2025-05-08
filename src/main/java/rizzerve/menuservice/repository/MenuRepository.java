package rizzerve.menuservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rizzerve.menuservice.model.MenuItem;

import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<MenuItem, UUID> {
    // Spring Data JPA will automatically implement basic CRUD operations
}
