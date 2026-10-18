package bo.edu.uagrm.backend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonacionConcretadaRequestDTO {

    @NotNull(message = "El ID de la solicitud de ayuda o necesidad es obligatorio")
    private Long solicitudId;

    @Min(value = 1, message = "La cantidad entregada debe ser mayor a cero")
    private Integer cantidadEntregada;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;
}
