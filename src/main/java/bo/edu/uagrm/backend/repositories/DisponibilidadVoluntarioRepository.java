package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.DisponibilidadVoluntario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisponibilidadVoluntarioRepository extends JpaRepository<DisponibilidadVoluntario, Long> {
    List<DisponibilidadVoluntario> findByPublicacionId(Long publicacionId);
    void deleteByPublicacionId(Long publicacionId);
}
