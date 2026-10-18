package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.AuditoriaResponseDTO;
import bo.edu.uagrm.backend.services.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/auditoria")
@Tag(name = "Auditoría de Publicaciones (Admin)", description = "Endpoints para consultar el historial estructurado de modificaciones, ediciones y cambios de estado (HU20)")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/publicaciones/{id}")
    @Operation(summary = "Historial de modificaciones de una publicación (HU20)", description = "Retorna el historial inmutable de cambios (edición de texto, cambios de estado, responsable, valor anterior vs nuevo) para una publicación específica")
    public ResponseEntity<Page<AuditoriaResponseDTO>> obtenerHistorialPorPublicacion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(auditoriaService.obtenerHistorialPorPublicacion(id, page, size));
    }

    @GetMapping("/general")
    @Operation(summary = "Historial general de auditoría del sistema (HU20)", description = "Retorna el registro global paginado de todas las operaciones sobre publicaciones realizadas en la plataforma")
    public ResponseEntity<Page<AuditoriaResponseDTO>> obtenerHistorialGeneral(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(auditoriaService.obtenerHistorialGeneral(page, size));
    }
}
