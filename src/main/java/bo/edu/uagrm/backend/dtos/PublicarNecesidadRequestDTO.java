package bo.edu.uagrm.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicarNecesidadRequestDTO {

    @NotBlank(message = "El título de la necesidad es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción de la necesidad es obligatoria")
    private String descripcion;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;
}
