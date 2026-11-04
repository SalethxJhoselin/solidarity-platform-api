package bo.edu.uagrm.backend.repositories.specifications;

import bo.edu.uagrm.backend.dtos.FiltroPublicacionDTO;
import bo.edu.uagrm.backend.enums.EstadoPublicacion;
import bo.edu.uagrm.backend.entities.Publicacion;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PublicacionSpecification {

    public static Specification<Publicacion> conFiltros(FiltroPublicacionDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Criterio 3: Solo publicaciones vigentes (estados Abierta o En proceso) y activas
            predicates.add(cb.isTrue(root.get("activo")));
            predicates.add(root.get("estado").in(EstadoPublicacion.ABIERTA, EstadoPublicacion.EN_PROCESO));

            if (filtro != null) {
                // Criterio 2: Búsqueda textual case-insensitive en título o descripción
                if (filtro.getQuery() != null && !filtro.getQuery().trim().isEmpty()) {
                    String pattern = "%" + filtro.getQuery().trim().toLowerCase() + "%";
                    Predicate tituloMatch = cb.like(cb.lower(root.get("titulo")), pattern);
                    Predicate descMatch = cb.like(cb.lower(root.get("descripcion")), pattern);
                    predicates.add(cb.or(tituloMatch, descMatch));
                }

                // Filtro por tipo de publicación (NECESIDAD, VOLUNTARIADO, DONACION, ACTIVIDAD_COLECTIVA)
                if (filtro.getTipo() != null) {
                    predicates.add(cb.equal(root.get("tipo"), filtro.getTipo()));
                }

                // Filtro por categoría
                if (filtro.getCategoriaId() != null) {
                    predicates.add(cb.equal(root.get("categoria").get("id"), filtro.getCategoriaId()));
                }

                // Filtro por tipo de recurso
                if (filtro.getTipoRecursoId() != null) {
                    predicates.add(cb.equal(root.get("tipoRecurso").get("id"), filtro.getTipoRecursoId()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
