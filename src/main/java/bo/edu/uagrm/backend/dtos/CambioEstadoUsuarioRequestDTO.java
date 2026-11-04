package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.enums.EstadoUsuario;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoUsuarioRequestDTO {
    @NotNull(message = "El nuevo estado del usuario es obligatorio")
    private EstadoUsuario estado;
}

