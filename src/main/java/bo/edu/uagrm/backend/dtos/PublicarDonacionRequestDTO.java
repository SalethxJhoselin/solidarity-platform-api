package bo.edu.uagrm.backend.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicarDonacionRequestDTO {

    @NotBlank(message = "El título de la donación es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción del recurso y su estado es obligatoria")
    private String descripcion;

    private Long categoriaId;

    @NotNull(message = "El tipo de recurso del catálogo es obligatorio")
    private Long tipoRecursoId;

    @NotNull(message = "La cantidad disponible es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser como mínimo 1 unidad")
    private Integer cantidad;
}
