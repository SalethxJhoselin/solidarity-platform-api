package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.ValidacionRequestDTO;
import bo.edu.uagrm.backend.dtos.ValidacionResponseDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.security.CurrentUser;
import bo.edu.uagrm.backend.services.ValidacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for academic student/teacher validation requests (HU5).
 */
@RestController
@RequestMapping({"/verification", "/validacion"})
@Tag(name = "Validación Académica", description = "Endpoints for academic profile registration and status inquiry (HU5)")
@SecurityRequirement(name = "BearerAuth")
public class ValidacionController {

    private final ValidacionService validacionService;

    public ValidacionController(ValidacionService validacionService) {
        this.validacionService = validacionService;
    }

    @PostMapping({"", "/", "/academic-record", "/registro-academico"})
    @Operation(summary = "Submit academic validation request (HU5)", description = "Submits university registration ID and member type (student/teacher)")
    public ResponseEntity<ValidacionResponseDTO> solicitarValidacion(
            @CurrentUser Usuario usuario,
            @Valid @RequestBody ValidacionRequestDTO requestDTO) {
        ValidacionResponseDTO response = validacionService.solicitarValidacion(usuario, requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/status", "/estado"})
    @Operation(summary = "Check academic validation status (HU5)", description = "Returns academic verification status of the authenticated user")
    public ResponseEntity<ValidacionResponseDTO> obtenerEstadoValidacion(@CurrentUser Usuario usuario) {
        return ResponseEntity.ok(validacionService.obtenerEstadoValidacion(usuario));
    }
}
