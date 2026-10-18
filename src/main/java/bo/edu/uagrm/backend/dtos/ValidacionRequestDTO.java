package bo.edu.uagrm.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidacionRequestDTO {
    @NotBlank(message = "El registro de la UAGRM es obligatorio")
    private String registroUagrm;

    @NotBlank(message = "El tipo es obligatorio (ESTUDIANTE/DOCENTE)")
    private String tipo;
}
