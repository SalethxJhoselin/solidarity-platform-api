package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.CategoriaPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaPublicacionRepository extends JpaRepository<CategoriaPublicacion, Long> {
    List<CategoriaPublicacion> findByActivoTrueOrderByIdAsc();
    List<CategoriaPublicacion> findAllByOrderByIdAsc();
    Optional<CategoriaPublicacion> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
