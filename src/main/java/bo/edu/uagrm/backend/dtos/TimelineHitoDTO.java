package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.enums.EstadoPublicacion;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineHitoDTO {
    private EstadoPublicacion estado;
    private String titulo;
    private String descripcion;
    private String usuarioResponsable;
    private LocalDateTime fecha;
    private boolean completado;
}
