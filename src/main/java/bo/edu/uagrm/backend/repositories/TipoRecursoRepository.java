package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.TipoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoRecursoRepository extends JpaRepository<TipoRecurso, Long> {
    List<TipoRecurso> findByActivoTrueOrderByIdAsc();
    List<TipoRecurso> findAllByOrderByIdAsc();
    Optional<TipoRecurso> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
