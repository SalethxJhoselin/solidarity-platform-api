package bo.edu.uagrm.backend.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentarioResponseDTO {
    private Long id;
    private Long publicacionId;
    private Long usuarioId;
    private String autorNombre;
    private String autorCorreo;
    private String autorFoto;
    private String contenido;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
