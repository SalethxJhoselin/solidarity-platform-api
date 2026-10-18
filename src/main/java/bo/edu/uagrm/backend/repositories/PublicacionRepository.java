package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.EstadoPublicacion;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.TipoPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long>, JpaSpecificationExecutor<Publicacion> {
    
    boolean existsByCategoriaId(Long categoriaId);
    
    boolean existsByTipoRecursoId(Long tipoRecursoId);
    
    List<Publicacion> findByUsuarioIdAndActivoTrueOrderByFechaCreacionDesc(Long usuarioId);
    
    List<Publicacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    
    List<Publicacion> findByActivoTrueOrderByFechaCreacionDesc();
    
    List<Publicacion> findByTipoAndActivoTrueOrderByFechaCreacionDesc(TipoPublicacion tipo);
    
    List<Publicacion> findByTipoAndEstadoAndActivoTrueOrderByFechaCreacionDesc(TipoPublicacion tipo, EstadoPublicacion estado);
}
