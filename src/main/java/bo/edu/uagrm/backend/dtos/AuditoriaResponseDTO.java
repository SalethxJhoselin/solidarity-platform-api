package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.entities.TipoAccionAuditoria;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaResponseDTO {
    private Long id;
    private Long publicacionId;
    private String publicacionTitulo;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioCorreo;
    private TipoAccionAuditoria tipoAccion;
    private String campoModificado;
    private String valorAnterior;
    private String valorNuevo;
    private String motivo;
    private LocalDateTime fechaRegistro;
}
