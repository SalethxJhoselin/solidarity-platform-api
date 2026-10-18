package bo.edu.uagrm.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicarVoluntariadoRequestDTO {

    @NotBlank(message = "El título de la oferta de voluntariado es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción de la oferta y áreas de experiencia es obligatoria")
    private String descripcion;

    @NotNull(message = "La categoría principal es obligatoria")
    private Long categoriaPrincipalId;

    private Set<Long> categoriasSecundariasIds;

    @Size(max = 255, message = "La descripción de disponibilidad no puede exceder 255 caracteres")
    private String disponibilidadHoraria;

    private List<DisponibilidadDTO> disponibilidades;
}
