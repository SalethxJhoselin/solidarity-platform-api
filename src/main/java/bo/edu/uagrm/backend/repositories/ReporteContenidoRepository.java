package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.ReporteContenido;
import bo.edu.uagrm.backend.enums.EstadoReporte;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Enterprise repository for {@link ReporteContenido} with entity graphs resolving reporter and reported targets in single SQL queries.
 */
@Repository
public interface ReporteContenidoRepository extends JpaRepository<ReporteContenido, Long> {

    @EntityGraph(attributePaths = {"denunciante", "publicacion", "comentario"})
    List<ReporteContenido> findByEstadoReporteOrderByFechaCreacionDesc(EstadoReporte estado);

    @EntityGraph(attributePaths = {"denunciante", "publicacion", "comentario"})
    List<ReporteContenido> findAllByOrderByFechaCreacionDesc();

    boolean existsByDenuncianteIdAndPublicacionId(Long denuncianteId, Long publicacionId);

    boolean existsByDenuncianteIdAndComentarioId(Long denuncianteId, Long comentarioId);
}
