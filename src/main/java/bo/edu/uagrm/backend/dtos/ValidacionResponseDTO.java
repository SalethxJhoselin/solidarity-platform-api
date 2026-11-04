package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.enums.EstadoValidacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionResponseDTO {
    private String mensaje;
    private boolean valido;
    private String registroAcademico;
    private String registroUagrm;
    private EstadoValidacion estado;
}

