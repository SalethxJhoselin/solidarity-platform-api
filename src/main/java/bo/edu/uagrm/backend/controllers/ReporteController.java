package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.ReporteRequestDTO;
import bo.edu.uagrm.backend.dtos.ReporteResponseDTO;
import bo.edu.uagrm.backend.entities.EstadoReporte;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import bo.edu.uagrm.backend.services.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes y Moderación", description = "Endpoints para denuncias de contenido inapropiado y moderación comunitaria (HU18)")
public class ReporteController {

    private final ReporteService reporteService;
    private final UsuarioRepository usuarioRepository;

    public ReporteController(ReporteService reporteService, UsuarioRepository usuarioRepository) {
        this.reporteService = reporteService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Reportar contenido inapropiado (HU18)", description = "Permite a un usuario denunciar una publicación o comentario por spam, fraude, ofensas, etc.")
    public ResponseEntity<ReporteResponseDTO> crearReporte(
            @Valid @RequestBody ReporteRequestDTO request,
            Authentication authentication) {
        Usuario usuario = getAuthenticatedUser(authentication);
        return new ResponseEntity<>(reporteService.crearReporte(usuario.getId(), request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Listar reportes de contenido (Admin)", description = "Permite al administrador ver la bandeja de reportes, opcionalmente filtrando por estado")
    public ResponseEntity<List<ReporteResponseDTO>> listarReportes(
            @RequestParam(required = false) EstadoReporte estado) {
        return ResponseEntity.ok(reporteService.listarReportes(estado));
    }

    @GetMapping("/bandeja")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Bandeja de moderación acumulada (HU19)", description = "Centraliza los reportes de contenido agrupados por publicación con conteo acumulado y motivos para moderación rápida")
    public ResponseEntity<List<bo.edu.uagrm.backend.dtos.BandejaReportesDTO>> obtenerBandejaModeracion() {
        return ResponseEntity.ok(reporteService.obtenerBandejaReportesAcumulados());
    }

    @PostMapping("/publicaciones/{id}/bloquear")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Dar de baja publicación por reportes (HU19)", description = "Acción de 1 clic para bloquear la publicación reportada y aprobar los reportes vinculados")
    public ResponseEntity<String> bloquearPublicacion(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Publicación dada de baja por violar normas comunitarias") String resolucion) {
        reporteService.bloquearPublicacionPorReportes(id, resolucion);
        return ResponseEntity.ok("Publicación ID " + id + " bloqueada exitosamente y reportes resueltos.");
    }

    @PostMapping("/publicaciones/{id}/descartar")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Descartar reportes de publicación (HU19)", description = "Acción de 1 clic para ignorar y descartar todos los reportes pendientes de una publicación")
    public ResponseEntity<String> descartarReportes(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Reportes descartados por falta de mérito o justificación") String motivoDescarte) {
        reporteService.descartarReportesDePublicacion(id, motivoDescarte);
        return ResponseEntity.ok("Reportes pendientes para la publicación ID " + id + " descartados correctamente.");
    }

    @PatchMapping("/{id}/resolver")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Resolver reporte individual de contenido (Admin)", description = "Cambia el estado del reporte a APROBADO_SANCIONADO o DESCARTADO")
    public ResponseEntity<ReporteResponseDTO> resolverReporte(
            @PathVariable Long id,
            @RequestParam EstadoReporte nuevoEstado,
            @RequestParam(required = false) String resolucionAdmin) {
        return ResponseEntity.ok(reporteService.resolverReporte(id, nuevoEstado, resolucionAdmin));
    }

    private Usuario getAuthenticatedUser(Authentication authentication) {
        String correo = authentication.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
}
