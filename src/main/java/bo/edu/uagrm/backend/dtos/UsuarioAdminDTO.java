package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.enums.*;

import bo.edu.uagrm.backend.entities.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAdminDTO {
    private String id;
    private Long usuarioId;
    private String name;
    private String email;
    private List<String> roles;
    private EstadoUsuario estado;
    private EstadoValidacion estadoValidacion;
    private boolean activo;

    public static UsuarioAdminDTO from(Usuario u, PerfilUsuario perfil, ValidacionAcademica validacion) {
        String name = u.getCorreo();
        if (perfil != null && perfil.getNombres() != null) {
            name = (perfil.getNombres() + (perfil.getApellidos() != null ? " " + perfil.getApellidos() : "")).trim();
        }

        List<String> rolesList = u.getRoles().stream()
                .map(r -> RolNombre.fromString(r.getNombre()).getCleanName())
                .collect(Collectors.toList());

        EstadoValidacion estVal = EstadoValidacion.VALIDADO;
        if (!bo.edu.uagrm.backend.services.AuthService.esDominioUagrm(u.getCorreo())) {
            estVal = (validacion != null && validacion.getEstadoValidacion() != null)
                    ? validacion.getEstadoValidacion()
                    : EstadoValidacion.PENDIENTE;
        }

        return UsuarioAdminDTO.builder()
                .id(String.valueOf(u.getId()))
                .usuarioId(u.getId())
                .name(name)
                .email(u.getCorreo())
                .roles(rolesList)
                .estado(u.getEstado())
                .estadoValidacion(estVal)
                .activo(u.getEstado() == EstadoUsuario.ACTIVO)
                .build();
    }
}

