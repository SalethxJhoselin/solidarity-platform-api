package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.BandejaReportesDTO;
import bo.edu.uagrm.backend.dtos.MensajeResponseDTO;
import bo.edu.uagrm.backend.dtos.ReporteRequestDTO;
import bo.edu.uagrm.backend.dtos.ReporteResponseDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.enums.EstadoReporte;
import bo.edu.uagrm.backend.security.CurrentUser;
import bo.edu.uagrm.backend.services.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for content moderation, reports, and administrative triage (HU18, HU19).
 */
@RestController
@RequestMapping({"/reports", "/reportes"})
@Tag(name = "Reportes y Moderación", description = "Endpoints for reporting inappropriate content and community moderation (HU18, HU19)")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping({"", "/"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Report inappropriate content (HU18)", description = "Allows registered users to report publications or comments violating community guidelines")
    public ResponseEntity<ReporteResponseDTO> crearReporte(
            @Valid @RequestBody ReporteRequestDTO request,
            @CurrentUser Usuario usuario) {
        return new ResponseEntity<>(reporteService.crearReporte(usuario.getId(), request), HttpStatus.CREATED);
    }

    @GetMapping({"", "/"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERADOR')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "List content reports (Admin/Moderator)", description = "Retrieves reported content inbox, optionally filtered by report status")
    public ResponseEntity<List<ReporteResponseDTO>> listarReportes(
            @RequestParam(required = false) EstadoReporte estado) {
        return ResponseEntity.ok(reporteService.listarReportes(estado));
    }

    @GetMapping({"/inbox", "/bandeja"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERADOR')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Moderation inbox with accumulated counts (HU19)", description = "Aggregates active reports grouped by publication with report counts for rapid moderation")
    public ResponseEntity<List<BandejaReportesDTO>> obtenerBandejaModeracion() {
        return ResponseEntity.ok(reporteService.obtenerBandejaReportesAcumulados());
    }

    @PostMapping({"/publications/{id}/block", "/publicaciones/{id}/bloquear"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERADOR')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Block reported publication (HU19)", description = "Blocks publication and resolves associated pending reports in a single atomic action")
    public ResponseEntity<MensajeResponseDTO> bloquearPublicacion(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Publicación dada de baja por violar normas comunitarias") String resolucion,
            @CurrentUser Usuario admin) {
        reporteService.bloquearPublicacionPorReportes(id, admin, resolucion);
        return ResponseEntity.ok(MensajeResponseDTO.of("Publicación ID " + id + " bloqueada exitosamente y reportes resueltos.", id, null));
    }

    @PostMapping({"/publications/{id}/dismiss", "/publicaciones/{id}/descartar"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERADOR')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Dismiss publication reports (HU19)", description = "Dismisses unfounded reports for a publication")
    public ResponseEntity<MensajeResponseDTO> descartarReportes(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Reportes descartados por falta de mérito o justificación") String motivoDescarte) {
        reporteService.descartarReportesDePublicacion(id, motivoDescarte);
        return ResponseEntity.ok(MensajeResponseDTO.of("Reportes pendientes para la publicación ID " + id + " descartados correctamente.", id, null));
    }

    @PatchMapping({"/{id}/resolve", "/{id}/resolver"})
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERADOR')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Resolve individual report (Admin/Moderator)", description = "Updates report status to APROBADO_SANCIONADO or DESCARTADO")
    public ResponseEntity<ReporteResponseDTO> resolverReporte(
            @PathVariable Long id,
            @RequestParam(required = false) EstadoReporte nuevoEstado,
            @RequestParam(required = false) Boolean descartar,
            @RequestParam(value = "resolucionAdmin", required = false) String resolucionAdmin,
            @RequestParam(value = "resolucion", required = false) String resolucion) {
        EstadoReporte estado = nuevoEstado;
        if (estado == null) {
            estado = Boolean.TRUE.equals(descartar) ? EstadoReporte.DESCARTADO : EstadoReporte.APROBADO_SANCIONADO;
        }
        String motivo = resolucionAdmin != null ? resolucionAdmin : resolucion;
        return ResponseEntity.ok(reporteService.resolverReporte(id, estado, motivo));
    }
}
