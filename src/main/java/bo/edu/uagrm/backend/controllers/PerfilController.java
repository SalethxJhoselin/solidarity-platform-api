package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.PerfilDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import bo.edu.uagrm.backend.services.PerfilService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/perfil")
public class PerfilController {

    private final PerfilService perfilService;
    private final UsuarioRepository usuarioRepository;

    public PerfilController(PerfilService perfilService, UsuarioRepository usuarioRepository) {
        this.perfilService = perfilService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping({"", "/mi-perfil"})
    public ResponseEntity<PerfilDTO> getPerfil(Authentication authentication) {
        Usuario usuario = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(perfilService.getPerfil(usuario.getId()));
    }

    @PutMapping({"", "/mi-perfil"})
    public ResponseEntity<PerfilDTO> updatePerfil(Authentication authentication, @RequestBody PerfilDTO perfilDTO) {
        Usuario usuario = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(perfilService.updatePerfil(usuario.getId(), perfilDTO));
    }

    private Usuario getAuthenticatedUser(Authentication authentication) {
        String correo = authentication.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
}
