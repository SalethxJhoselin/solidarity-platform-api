package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.TokenRecuperacionContrasena;
import bo.edu.uagrm.backend.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TokenRecuperacionContrasenaRepository extends JpaRepository<TokenRecuperacionContrasena, Long> {
    Optional<TokenRecuperacionContrasena> findByHashToken(String hashToken);
    List<TokenRecuperacionContrasena> findByUsuarioAndFechaUsoIsNull(Usuario usuario);
}

