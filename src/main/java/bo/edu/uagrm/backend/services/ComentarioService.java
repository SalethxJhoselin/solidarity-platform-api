package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.ComentarioRequestDTO;
import bo.edu.uagrm.backend.dtos.ComentarioResponseDTO;
import bo.edu.uagrm.backend.entities.Comentario;
import bo.edu.uagrm.backend.entities.PerfilUsuario;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.ComentarioRepository;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             PublicacionRepository publicacionRepository,
                             UsuarioRepository usuarioRepository,
                             PerfilUsuarioRepository perfilUsuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
    }

    @Transactional
    public ComentarioResponseDTO agregarComentario(Long publicacionId, Long usuarioId, ComentarioRequestDTO request) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con ID: " + publicacionId));

        if (!publicacion.getActivo()) {
            throw new BadRequestException("No se pueden agregar comentarios a una publicación inactiva o cancelada");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        Comentario comentario = Comentario.builder()
                .publicacion(publicacion)
                .usuario(usuario)
                .contenido(request.getContenido().trim())
                .activo(true)
                .build();

        comentario = comentarioRepository.save(comentario);
        return mapToDTO(comentario);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> obtenerComentariosPorPublicacion(Long publicacionId) {
        if (!publicacionRepository.existsById(publicacionId)) {
            throw new ResourceNotFoundException("Publicación no encontrada con ID: " + publicacionId);
        }

        // Criterio 1: Ordenados cronológicamente desde el más antiguo al más reciente
        return comentarioRepository.findByPublicacionIdAndActivoTrueOrderByFechaCreacionAsc(publicacionId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComentarioResponseDTO editarComentario(Long comentarioId, Long usuarioId, boolean esAdmin, ComentarioRequestDTO request) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con ID: " + comentarioId));

        // Criterio 2: Validar que la petición provenga del autor o admin
        if (!comentario.getUsuario().getId().equals(usuarioId) && !esAdmin) {
            throw new BadRequestException("No tiene permisos para modificar este comentario");
        }

        comentario.setContenido(request.getContenido().trim());
        comentario = comentarioRepository.save(comentario);
        return mapToDTO(comentario);
    }

    @Transactional
    public void eliminarComentario(Long comentarioId, Long usuarioId, boolean esAdmin) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con ID: " + comentarioId));

        // Criterio 2: Validar que la petición provenga del autor o admin
        if (!comentario.getUsuario().getId().equals(usuarioId) && !esAdmin) {
            throw new BadRequestException("No tiene permisos para eliminar este comentario");
        }

        comentarioRepository.delete(comentario);
    }

    public ComentarioResponseDTO mapToDTO(Comentario c) {
        Usuario u = c.getUsuario();
        PerfilUsuario perfil = perfilUsuarioRepository.findById(u.getId()).orElse(null);

        String nombreAutor = (perfil != null) ? perfil.getNombres() + " " + perfil.getApellidos() : u.getCorreo();
        String fotoAutor = (perfil != null) ? perfil.getUrlFotoPerfil() : null;

        return ComentarioResponseDTO.builder()
                .id(c.getId())
                .publicacionId(c.getPublicacion().getId())
                .usuarioId(u.getId())
                .autorNombre(nombreAutor)
                .autorCorreo(u.getCorreo())
                .autorFoto(fotoAutor)
                .contenido(c.getContenido())
                .activo(c.getActivo())
                .fechaCreacion(c.getFechaCreacion())
                .fechaActualizacion(c.getFechaActualizacion())
                .build();
    }
}
