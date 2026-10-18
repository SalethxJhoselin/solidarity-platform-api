package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.ArchivoAdjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArchivoAdjuntoRepository extends JpaRepository<ArchivoAdjunto, Long> {
    List<ArchivoAdjunto> findByPublicacionId(Long publicacionId);
    Optional<ArchivoAdjunto> findByNombreAlmacenado(String nombreAlmacenado);
}
