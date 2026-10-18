package bo.edu.uagrm.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PerfilDTO {
    private String nombres;
    private String apellidos;
    private String descripcion;
    private String facultad;
    private String carrera;
    private String urlFotoPerfil;
    private String telefono;
}
