package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.enums.EstadoPublicacion;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarPublicacionRequestDTO {

    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String titulo;

    private String descripcion;

    private Long categoriaId;

    private Set<Long> categoriasSecundariasIds;

    private Long tipoRecursoId;

    private Integer cantidadTotal;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private String lugar;

    private String disponibilidadHoraria;

    private List<DisponibilidadDTO> disponibilidades;

    private EstadoPublicacion estado;
}
