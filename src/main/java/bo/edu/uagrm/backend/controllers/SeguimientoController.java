package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.SeguimientoSolicitudDTO;
import bo.edu.uagrm.backend.services.TrazabilidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seguimiento")
@Tag(name = "Trazabilidad y Seguimiento", description = "Endpoints para consultar el ciclo de vida y la línea de tiempo (Timeline) de solicitudes de ayuda (HU21)")
public class SeguimientoController {

    private final TrazabilidadService trazabilidadService;

    public SeguimientoController(TrazabilidadService trazabilidadService) {
        this.trazabilidadService = trazabilidadService;
    }

    @GetMapping("/solicitudes/{solicitudId}")
    @Operation(summary = "Consultar seguimiento y ciclo de vida de solicitud (HU21)", description = "Retorna la línea de tiempo gráfica (ABIERTA -> EN_PROCESO -> RESUELTA) con los hitos, fechas y responsables")
    public ResponseEntity<SeguimientoSolicitudDTO> obtenerSeguimientoSolicitud(@PathVariable Long solicitudId) {
        return ResponseEntity.ok(trazabilidadService.obtenerSeguimiento(solicitudId));
    }
}
