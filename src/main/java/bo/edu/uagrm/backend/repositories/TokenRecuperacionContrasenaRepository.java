package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.TokenRecuperacionContrasena;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenRecuperacionContrasenaRepository extends JpaRepository<TokenRecuperacionContrasena, Long> {
    Optional<TokenRecuperacionContrasena> findByHashToken(String hashToken);
}
