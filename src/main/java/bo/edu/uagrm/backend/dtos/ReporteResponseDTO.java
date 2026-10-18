package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.entities.EstadoReporte;
import bo.edu.uagrm.backend.entities.MotivoReporte;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteResponseDTO {
    private Long id;
    private Long denuncianteId;
    private String denuncianteNombre;
    private String denuncianteCorreo;

    private Long publicacionId;
    private String publicacionTitulo;

    private Long comentarioId;
    private String comentarioTexto;

    private MotivoReporte motivo;
    private String detalles;
    private EstadoReporte estadoReporte;
    private String resolucionAdmin;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaResolucion;

    @com.fasterxml.jackson.annotation.JsonProperty("estado")
    public EstadoReporte getEstado() {
        return estadoReporte;
    }
}
