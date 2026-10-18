package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.entities.MotivoReporte;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteRequestDTO {

    private Long publicacionId;

    private Long comentarioId;

    @NotNull(message = "El motivo del reporte es obligatorio")
    private MotivoReporte motivo;

    @Size(max = 1000, message = "Los detalles no pueden exceder 1000 caracteres")
    private String detalles;
}
