package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.DonacionConcretadaRequestDTO;
import bo.edu.uagrm.backend.dtos.DonacionConcretadaResponseDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.security.CurrentUser;
import bo.edu.uagrm.backend.services.DonacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for confirmed donation records and resolution tracking (HU17).
 */
@RestController
@RequestMapping({"/donations", "/donaciones"})
@Tag(name = "Donaciones Concretadas", description = "Endpoints for registering fulfilled donations and transitioning requests to RESUELTA (HU17)")
public class DonacionController {

    private final DonacionService donacionService;

    public DonacionController(DonacionService donacionService) {
        this.donacionService = donacionService;
    }

    @PostMapping({"/fulfill", "/concretar"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Register confirmed donation (HU17)", description = "Links the donor to the help request and automatically transitions its status to RESUELTA")
    public ResponseEntity<DonacionConcretadaResponseDTO> registrarEntrega(
            @Valid @RequestBody DonacionConcretadaRequestDTO request,
            @CurrentUser Usuario usuario) {
        return new ResponseEntity<>(donacionService.registrarEntregaAyuda(usuario.getId(), request), HttpStatus.CREATED);
    }

    @GetMapping({"/request/{solicitudId}", "/solicitud/{solicitudId}"})
    @Operation(summary = "List donation receipts for a given help request")
    public ResponseEntity<List<DonacionConcretadaResponseDTO>> listarPorSolicitud(@PathVariable Long solicitudId) {
        return ResponseEntity.ok(donacionService.listarEntregasPorSolicitud(solicitudId));
    }

    @GetMapping({"/my-fulfillments", "/mis-entregas"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Consult user's own confirmed donations history")
    public ResponseEntity<List<DonacionConcretadaResponseDTO>> listarMisEntregas(@CurrentUser Usuario usuario) {
        return ResponseEntity.ok(donacionService.listarMisDonacionesEntregadas(usuario.getId()));
    }
}
