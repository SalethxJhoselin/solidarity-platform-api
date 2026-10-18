package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.TrazabilidadSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrazabilidadSolicitudRepository extends JpaRepository<TrazabilidadSolicitud, Long> {
    List<TrazabilidadSolicitud> findBySolicitudIdOrderByFechaHitoAsc(Long solicitudId);
}
