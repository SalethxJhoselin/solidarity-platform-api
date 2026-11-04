package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.ComentarioRequestDTO;
import bo.edu.uagrm.backend.dtos.ComentarioResponseDTO;
import bo.edu.uagrm.backend.dtos.MensajeResponseDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.security.CurrentUser;
import bo.edu.uagrm.backend.services.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for interaction and discussion comments on publications (HU15).
 */
@RestController
@RequestMapping({"/comments", "/comentarios"})
@Tag(name = "Comentarios", description = "Endpoints for community comments and thread interaction on publications (HU15)")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @PostMapping({"", "/"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Post a comment on a publication (HU15)")
    public ResponseEntity<ComentarioResponseDTO> agregarComentario(
            @Valid @RequestBody ComentarioRequestDTO request,
            @CurrentUser Usuario usuario) {
        if (request.getPublicacionId() == null) {
            throw new BadRequestException("Must specify the target publication ID in the request body");
        }
        return new ResponseEntity<>(comentarioService.agregarComentario(request.getPublicacionId(), usuario.getId(), request), HttpStatus.CREATED);
    }

    @GetMapping({"/publication/{publicacionId}", "/publicacion/{publicacionId}"})
    @Operation(summary = "List comments for a publication")
    public ResponseEntity<List<ComentarioResponseDTO>> listarComentariosPorPublicacion(@PathVariable Long publicacionId) {
        return ResponseEntity.ok(comentarioService.obtenerComentariosPorPublicacion(publicacionId));
    }

    @PutMapping("/{comentarioId}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Edit comment (HU15)")
    public ResponseEntity<ComentarioResponseDTO> editarComentario(
            @PathVariable Long comentarioId,
            @Valid @RequestBody ComentarioRequestDTO request,
            @CurrentUser Usuario usuario) {
        return ResponseEntity.ok(comentarioService.editarComentario(comentarioId, usuario.getId(), usuario.esAdmin(), request));
    }

    @DeleteMapping("/{comentarioId}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Delete comment (HU15)")
    public ResponseEntity<MensajeResponseDTO> eliminarComentario(
            @PathVariable Long comentarioId,
            @CurrentUser Usuario usuario) {
        comentarioService.eliminarComentario(comentarioId, usuario.getId(), usuario.esAdmin());
        return ResponseEntity.ok(MensajeResponseDTO.of("Comentario eliminado correctamente", comentarioId, null));
    }
}
