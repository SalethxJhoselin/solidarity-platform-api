package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.entities.ValidacionAcademica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ValidacionAcademicaRepository extends JpaRepository<ValidacionAcademica, Long> {
    boolean existsByRegistroUagrm(String registroUagrm);
    Optional<ValidacionAcademica> findFirstByUsuarioOrderByIdDesc(Usuario usuario);
    List<ValidacionAcademica> findByUsuario(Usuario usuario);
    Optional<ValidacionAcademica> findByUsuarioId(Long usuarioId);
    List<ValidacionAcademica> findAllByOrderByFechaCreacionDesc();
}

