package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.ValidacionRequestDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import bo.edu.uagrm.backend.services.ValidacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/validacion")
public class ValidacionController {

    private final ValidacionService validacionService;
    private final UsuarioRepository usuarioRepository;

    public ValidacionController(ValidacionService validacionService, UsuarioRepository usuarioRepository) {
        this.validacionService = validacionService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<String> solicitarValidacion(Authentication authentication, @Valid @RequestBody ValidacionRequestDTO requestDTO) {
        Usuario usuario = getAuthenticatedUser(authentication);
        validacionService.solicitarValidacion(usuario, requestDTO);
        return ResponseEntity.ok("Solicitud de validación enviada correctamente");
    }

    private Usuario getAuthenticatedUser(Authentication authentication) {
        String correo = authentication.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
}
