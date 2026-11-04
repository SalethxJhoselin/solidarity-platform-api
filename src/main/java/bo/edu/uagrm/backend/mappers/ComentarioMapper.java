package bo.edu.uagrm.backend.mappers;

import bo.edu.uagrm.backend.dtos.ComentarioResponseDTO;
import bo.edu.uagrm.backend.entities.Comentario;
import bo.edu.uagrm.backend.entities.PerfilUsuario;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import org.springframework.stereotype.Component;

/**
 * Enterprise mapper responsible for transforming {@link Comentario} entities
 * into {@link ComentarioResponseDTO} representations.
 */
@Component
public class ComentarioMapper {

    private final PerfilUsuarioRepository perfilUsuarioRepository;

    public ComentarioMapper(PerfilUsuarioRepository perfilUsuarioRepository) {
        this.perfilUsuarioRepository = perfilUsuarioRepository;
    }

    public ComentarioResponseDTO toDTO(Comentario c) {
        if (c == null) {
            return null;
        }

        Usuario u = c.getUsuario();
        PerfilUsuario perfil = (u != null)
                ? perfilUsuarioRepository.findById(u.getId()).orElse(null)
                : null;

        String autorNombre = (perfil != null)
                ? perfil.getNombres() + " " + perfil.getApellidos()
                : (u != null ? u.getCorreo() : "Usuario");
        String autorFoto = (perfil != null) ? perfil.getUrlFotoPerfil() : null;
        String autorCorreo = (u != null) ? u.getCorreo() : null;

        return ComentarioResponseDTO.builder()
                .id(c.getId())
                .publicacionId(c.getPublicacion() != null ? c.getPublicacion().getId() : null)
                .usuarioId(u != null ? u.getId() : null)
                .autorNombre(autorNombre)
                .autorCorreo(autorCorreo)
                .autorFoto(autorFoto)
                .contenido(c.getContenido())
                .activo(c.getActivo())
                .fechaCreacion(c.getFechaCreacion())
                .fechaActualizacion(c.getFechaActualizacion())
                .build();
    }
}

