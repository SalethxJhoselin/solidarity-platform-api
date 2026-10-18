package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.ValidacionAcademica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidacionAcademicaRepository extends JpaRepository<ValidacionAcademica, Long> {
    boolean existsByRegistroUagrm(String registroUagrm);
}
