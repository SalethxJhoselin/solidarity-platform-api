package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.entities.EstadoPublicacion;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoSolicitudDTO {
    private Long solicitudId;
    private String titulo;
    private String descripcion;
    private EstadoPublicacion estadoActual;
    private String solicitanteNombre;
    private String solicitanteCorreo;
    private String categoriaNombre;
    private LocalDateTime fechaCreacion;
    private List<TimelineHitoDTO> lineaDeTiempo;

    @com.fasterxml.jackson.annotation.JsonProperty("hitos")
    public List<TimelineHitoDTO> getHitos() {
        return lineaDeTiempo;
    }
}
