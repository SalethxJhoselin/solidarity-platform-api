package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.EstadoReporte;
import bo.edu.uagrm.backend.entities.ReporteContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteContenidoRepository extends JpaRepository<ReporteContenido, Long> {
    List<ReporteContenido> findByEstadoReporteOrderByFechaCreacionDesc(EstadoReporte estado);
    List<ReporteContenido> findAllByOrderByFechaCreacionDesc();
    boolean existsByDenuncianteIdAndPublicacionId(Long denuncianteId, Long publicacionId);
    boolean existsByDenuncianteIdAndComentarioId(Long denuncianteId, Long comentarioId);
}
