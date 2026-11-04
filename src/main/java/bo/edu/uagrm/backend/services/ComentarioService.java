package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.ComentarioRequestDTO;
import bo.edu.uagrm.backend.dtos.ComentarioResponseDTO;
import bo.edu.uagrm.backend.entities.Comentario;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.mappers.ComentarioMapper;
import bo.edu.uagrm.backend.repositories.ComentarioRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Enterprise service responsible for publication discussion comments lifecycle (HU15).
 */
@Slf4j
@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComentarioMapper comentarioMapper;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             PublicacionRepository publicacionRepository,
                             UsuarioRepository usuarioRepository,
                             ComentarioMapper comentarioMapper) {
        this.comentarioRepository = comentarioRepository;
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.comentarioMapper = comentarioMapper;
    }

    @Transactional
    public ComentarioResponseDTO agregarComentario(Long publicacionId, Long usuarioId, ComentarioRequestDTO request) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con ID: " + publicacionId));

        if (!Boolean.TRUE.equals(publicacion.getActivo())) {
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
        log.info("New comment {} posted on publication {} by user {}", comentario.getId(), publicacionId, usuarioId);
        return comentarioMapper.toDTO(comentario);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> obtenerComentariosPorPublicacion(Long publicacionId) {
        if (!publicacionRepository.existsById(publicacionId)) {
            throw new ResourceNotFoundException("Publicación no encontrada con ID: " + publicacionId);
        }
        return comentarioRepository.findByPublicacionIdAndActivoTrueOrderByFechaCreacionAsc(publicacionId).stream()
                .map(comentarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComentarioResponseDTO editarComentario(Long comentarioId, Long usuarioId, boolean esAdmin, ComentarioRequestDTO request) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con ID: " + comentarioId));

        if (!comentario.getUsuario().getId().equals(usuarioId) && !esAdmin) {
            throw new AccessDeniedException("No tiene permisos para modificar este comentario");
        }

        comentario.setContenido(request.getContenido().trim());
        comentario = comentarioRepository.save(comentario);
        log.info("Comment {} updated by user {}", comentarioId, usuarioId);
        return comentarioMapper.toDTO(comentario);
    }

    @Transactional
    public void eliminarComentario(Long comentarioId, Long usuarioId, boolean esAdmin) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con ID: " + comentarioId));

        if (!comentario.getUsuario().getId().equals(usuarioId) && !esAdmin) {
            throw new AccessDeniedException("No tiene permisos para eliminar este comentario");
        }

        comentario.setActivo(false);
        comentarioRepository.save(comentario);
        log.info("Comment {} soft-deleted by user {}", comentarioId, usuarioId);
    }
}
