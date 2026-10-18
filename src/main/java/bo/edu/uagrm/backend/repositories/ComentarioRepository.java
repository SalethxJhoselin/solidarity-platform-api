package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByPublicacionIdAndActivoTrueOrderByFechaCreacionAsc(Long publicacionId);
    List<Comentario> findByPublicacionIdOrderByFechaCreacionAsc(Long publicacionId);
}
