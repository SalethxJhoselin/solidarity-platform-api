package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.DonacionConcretadaRequestDTO;
import bo.edu.uagrm.backend.dtos.DonacionConcretadaResponseDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import bo.edu.uagrm.backend.services.DonacionService;
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
@RequestMapping("/api/v1/donaciones")
@Tag(name = "Donaciones Concretadas", description = "Endpoints para registro formal de ayuda entregada y trazabilidad a RESUELTA (HU17)")
public class DonacionController {

    private final DonacionService donacionService;
    private final UsuarioRepository usuarioRepository;

    public DonacionController(DonacionService donacionService, UsuarioRepository usuarioRepository) {
        this.donacionService = donacionService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/concretar")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Registrar donación o ayuda concretada (HU17)", description = "Enlaza al donante con la solicitud de ayuda y actualiza automáticamente su estado a RESUELTA")
    public ResponseEntity<DonacionConcretadaResponseDTO> registrarEntrega(
            @Valid @RequestBody DonacionConcretadaRequestDTO request,
            Authentication authentication) {
        Usuario usuario = getAuthenticatedUser(authentication);
        return new ResponseEntity<>(donacionService.registrarEntregaAyuda(usuario.getId(), request), HttpStatus.CREATED);
    }

    @GetMapping("/solicitud/{solicitudId}")
    @Operation(summary = "Listar constancias de entrega de una solicitud")
    public ResponseEntity<List<DonacionConcretadaResponseDTO>> listarPorSolicitud(@PathVariable Long solicitudId) {
        return ResponseEntity.ok(donacionService.listarEntregasPorSolicitud(solicitudId));
    }

    @GetMapping("/mis-entregas")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Consultar historial propio de donaciones entregadas")
    public ResponseEntity<List<DonacionConcretadaResponseDTO>> listarMisEntregas(Authentication authentication) {
        Usuario usuario = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(donacionService.listarMisDonacionesEntregadas(usuario.getId()));
    }

    private Usuario getAuthenticatedUser(Authentication authentication) {
        String correo = authentication.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
}
