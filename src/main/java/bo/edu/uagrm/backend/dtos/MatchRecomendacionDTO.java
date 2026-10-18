package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.entities.TipoPublicacion;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchRecomendacionDTO {
    private Long publicacionId;
    private TipoPublicacion tipo;
    private String titulo;
    private String descripcion;
    private String nombreOferente;
    private String correoOferente;
    private String carrera;
    private String facultad;
    private String telefono;
    private String fotoPerfil;
    private String categoriaNombre;
    private String tipoRecursoNombre;
    private Integer cantidadDisponible;
    private String disponibilidadHoraria;

    // Métricas del algoritmo de Inteligencia Artificial (LightGBM)
    private Double porcentajeCompatibilidad; // ej. 92.5
    private String etiquetaCompatibilidad; // ej. "Match 93%"
    private List<String> factoresCoincidencia; // Razones de compatibilidad calculadas
}
