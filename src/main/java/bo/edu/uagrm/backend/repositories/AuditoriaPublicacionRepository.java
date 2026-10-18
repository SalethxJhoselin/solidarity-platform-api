package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.AuditoriaPublicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaPublicacionRepository extends JpaRepository<AuditoriaPublicacion, Long> {
    Page<AuditoriaPublicacion> findByPublicacionId(Long publicacionId, Pageable pageable);
    List<AuditoriaPublicacion> findByPublicacionIdOrderByFechaRegistroDesc(Long publicacionId);
    Page<AuditoriaPublicacion> findAllByOrderByFechaRegistroDesc(Pageable pageable);
}
