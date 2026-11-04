package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.enums.EstadoUsuario;
import bo.edu.uagrm.backend.enums.EstadoValidacion;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Long usuarioId;
    private String correo;
    private List<String> roles;
    private EstadoUsuario estado;
    private EstadoValidacion estadoValidacion;

    public AuthResponseDTO(String token, Long usuarioId, String correo, List<String> roles) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.correo = correo;
        this.roles = roles;
        this.estado = EstadoUsuario.ACTIVO;
        this.estadoValidacion = EstadoValidacion.VALIDADO;
    }

    @JsonProperty("id")
    public Long getId() {
        return usuarioId;
    }
}

