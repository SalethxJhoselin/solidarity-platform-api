package bo.edu.uagrm.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentarioRequestDTO {

    private Long publicacionId;

    @NotBlank(message = "El contenido del comentario no puede estar vacío")
    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String contenido;
}
