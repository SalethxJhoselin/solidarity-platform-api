package bo.edu.uagrm.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicarActividadColectivaRequestDTO {

    @NotBlank(message = "El título de la actividad colectiva es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción de la actividad o campaña es obligatoria")
    private String descripcion;

    private Long categoriaId;

    @NotNull(message = "La fecha y hora de inicio son obligatorias")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha y hora de fin son obligatorias")
    private LocalDateTime fechaFin;

    @NotBlank(message = "El lugar del evento es obligatorio")
    @Size(max = 255, message = "El lugar no puede exceder 255 caracteres")
    private String lugar;
}
