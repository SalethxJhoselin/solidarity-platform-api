package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.PerfilDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.security.CurrentUser;
import bo.edu.uagrm.backend.services.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile management.
 */
@RestController
@RequestMapping({"/profile", "/perfil"})
@Tag(name = "Perfil de Usuario", description = "Endpoints for managing user personal profile data")
@SecurityRequirement(name = "BearerAuth")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping({"", "/", "/my-profile", "/mi-perfil"})
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<PerfilDTO> getPerfil(@CurrentUser Usuario usuario) {
        return ResponseEntity.ok(perfilService.getPerfil(usuario.getId()));
    }

    @PutMapping({"", "/", "/my-profile", "/mi-perfil"})
    @Operation(summary = "Update current user profile")
    public ResponseEntity<PerfilDTO> updatePerfil(@CurrentUser Usuario usuario, @RequestBody PerfilDTO perfilDTO) {
        return ResponseEntity.ok(perfilService.updatePerfil(usuario.getId(), perfilDTO));
    }
}
