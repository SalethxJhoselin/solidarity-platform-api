package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.enums.*;

import bo.edu.uagrm.backend.entities.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionAdminDTO {
    private Long id;
    private Long usuarioId;
    private String correo;
    private String nombre;
    private String registroUagrm;
    private String identificadorInstitucional;
    private TipoMiembro tipo;
    private TipoMiembro tipoMiembro;
    private TipoVerificacion tipoVerificacion;
    private EstadoValidacion estadoValidacion;
    private String verificadoPor;
    private LocalDateTime fechaVerificacion;
    private LocalDateTime fechaCreacion;

    public static ValidacionAdminDTO from(ValidacionAcademica v, PerfilUsuario perfil) {
        String nombre = (perfil != null && perfil.getNombres() != null)
                ? (perfil.getNombres() + " " + (perfil.getApellidos() != null ? perfil.getApellidos() : "")).trim()
                : v.getUsuario().getCorreo();

        return ValidacionAdminDTO.builder()
                .id(v.getId())
                .usuarioId(v.getUsuario().getId())
                .correo(v.getUsuario().getCorreo())
                .nombre(nombre)
                .registroUagrm(v.getRegistroUagrm())
                .identificadorInstitucional(v.getIdentificadorInstitucional() != null ? v.getIdentificadorInstitucional() : v.getRegistroUagrm())
                .tipo(v.getTipo())
                .tipoMiembro(v.getTipoMiembro() != null ? v.getTipoMiembro() : v.getTipo())
                .tipoVerificacion(v.getTipoVerificacion())
                .estadoValidacion(v.getEstadoValidacion())
                .verificadoPor(v.getVerificadoPor() != null ? v.getVerificadoPor().getCorreo() : null)
                .fechaVerificacion(v.getFechaVerificacion())
                .fechaCreacion(v.getFechaCreacion())
                .build();
    }
}

