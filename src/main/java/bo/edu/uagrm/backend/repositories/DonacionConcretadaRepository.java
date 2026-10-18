package bo.edu.uagrm.backend.repositories;

import bo.edu.uagrm.backend.entities.DonacionConcretada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonacionConcretadaRepository extends JpaRepository<DonacionConcretada, Long> {
    List<DonacionConcretada> findBySolicitudIdOrderByFechaEntregaDesc(Long solicitudId);
    List<DonacionConcretada> findByDonanteIdOrderByFechaEntregaDesc(Long donanteId);
}
