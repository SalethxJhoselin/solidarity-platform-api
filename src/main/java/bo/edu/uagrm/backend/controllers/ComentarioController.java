package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.ComentarioRequestDTO;
import bo.edu.uagrm.backend.dtos.ComentarioResponseDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import bo.edu.uagrm.backend.services.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Comentarios", description = "Endpoints para interacción y comentarios en publicaciones (HU15)")
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final UsuarioRepository usuarioRepository;

    public ComentarioController(ComentarioService comentarioService, UsuarioRepository usuarioRepository) {
        this.comentarioService = comentarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/api/v1/publicaciones/{publicacionId}/comentarios")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Agregar comentario a una publicación (HU15)")
    public ResponseEntity<ComentarioResponseDTO> agregarComentario(
            @PathVariable Long publicacionId,
            @Valid @RequestBody ComentarioRequestDTO request,
            Authentication authentication) {
        Usuario usuario = getAuthenticatedUser(authentication);
        return new ResponseEntity<>(comentarioService.agregarComentario(publicacionId, usuario.getId(), request), HttpStatus.CREATED);
    }

    @GetMapping("/api/v1/publicaciones/{publicacionId}/comentarios")
    @Operation(summary = "Listar comentarios de una publicación (Orden cronológico)")
    public ResponseEntity<List<ComentarioResponseDTO>> listarComentarios(@PathVariable Long publicacionId) {
        return ResponseEntity.ok(comentarioService.obtenerComentariosPorPublicacion(publicacionId));
    }

    @PutMapping("/api/v1/comentarios/{comentarioId}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Editar comentario propio (HU15)")
    public ResponseEntity<ComentarioResponseDTO> editarComentario(
            @PathVariable Long comentarioId,
            @Valid @RequestBody ComentarioRequestDTO request,
            Authentication authentication) {
        Usuario usuario = getAuthenticatedUser(authentication);
        boolean admin = esAdmin(authentication);
        return ResponseEntity.ok(comentarioService.editarComentario(comentarioId, usuario.getId(), admin, request));
    }

    @DeleteMapping("/api/v1/comentarios/{comentarioId}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Eliminar comentario propio (HU15)")
    public ResponseEntity<String> eliminarComentario(
            @PathVariable Long comentarioId,
            Authentication authentication) {
        Usuario usuario = getAuthenticatedUser(authentication);
        boolean admin = esAdmin(authentication);
        comentarioService.eliminarComentario(comentarioId, usuario.getId(), admin);
        return ResponseEntity.ok("Comentario eliminado correctamente");
    }

    private Usuario getAuthenticatedUser(Authentication authentication) {
        String correo = authentication.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }

    private boolean esAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
