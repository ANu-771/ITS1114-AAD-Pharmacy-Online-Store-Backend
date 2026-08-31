package lk.ijse.pharmacy_backend.repository;

import lk.ijse.pharmacy_backend.entity.Role;
import lk.ijse.pharmacy_backend.enumiration.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(UserRole name);
}
