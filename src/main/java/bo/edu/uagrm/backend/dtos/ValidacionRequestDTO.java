package bo.edu.uagrm.backend.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidacionRequestDTO {
    @NotBlank(message = "El registro de la UAGRM es obligatorio")
    @JsonAlias({"registroAcademico", "registro"})
    private String registroUagrm;

    @JsonAlias({"tipoMiembro", "memberType"})
    private String tipo = "ESTUDIANTE";
}

