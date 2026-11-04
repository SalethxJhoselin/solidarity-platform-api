package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.enums.DiaSemana;
import bo.edu.uagrm.backend.enums.Turno;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadDTO {
    @NotNull(message = "El día de la semana es obligatorio")
    private DiaSemana diaSemana;

    @NotNull(message = "El turno es obligatorio")
    private Turno turno;
}
