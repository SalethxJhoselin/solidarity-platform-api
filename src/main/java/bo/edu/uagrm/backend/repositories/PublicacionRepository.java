package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.enums.EstadoPublicacion;
import bo.edu.uagrm.backend.enums.TipoPublicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Enterprise repository for {@link Publicacion} entities with optimized eager
 * fetch graphs to avoid N+1 query overhead.
 */
@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long>, JpaSpecificationExecutor<Publicacion> {

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByTipoRecursoId(Long tipoRecursoId);

    @EntityGraph(attributePaths = { "usuario", "categoria", "tipoRecurso" })
    List<Publicacion> findByUsuarioIdAndActivoTrueOrderByFechaCreacionDesc(Long usuarioId);

    @EntityGraph(attributePaths = { "usuario", "categoria", "tipoRecurso" })
    List<Publicacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    @EntityGraph(attributePaths = { "usuario", "categoria", "tipoRecurso" })
    List<Publicacion> findByActivoTrueOrderByFechaCreacionDesc();

    @EntityGraph(attributePaths = { "usuario", "categoria", "tipoRecurso" })
    List<Publicacion> findByTipoAndActivoTrueOrderByFechaCreacionDesc(TipoPublicacion tipo);

    @EntityGraph(attributePaths = { "usuario", "categoria", "tipoRecurso" })
    List<Publicacion> findByTipoAndEstadoAndActivoTrueOrderByFechaCreacionDesc(TipoPublicacion tipo,
            EstadoPublicacion estado);

    @Override
    @EntityGraph(attributePaths = { "usuario", "categoria", "tipoRecurso" })
    Page<Publicacion> findAll(@Nullable Specification<Publicacion> spec, Pageable pageable);
}
