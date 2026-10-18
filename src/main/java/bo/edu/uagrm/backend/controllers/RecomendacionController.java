package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.MatchRecomendacionDTO;
import bo.edu.uagrm.backend.services.MatchRecomendacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recomendaciones")
@Tag(name = "Recomendaciones Inteligentes (Match)", description = "Endpoints para el motor de recomendación inteligente de ofertas de voluntariado y donaciones compatibles con solicitudes (HU22)")
public class RecomendacionController {

    private final MatchRecomendacionService recomendacionService;

    public RecomendacionController(MatchRecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    @GetMapping("/match/{solicitudId}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Obtener mejores matches para una solicitud (HU22)", description = "Calcula la afinidad multivariable (categoría, afinidad académica UAGRM de facultad/carrera, horarios, insumos y coincidencia semántica) retornando el Top 3 con porcentaje de compatibilidad")
    public ResponseEntity<List<MatchRecomendacionDTO>> obtenerMejoresMatches(
            @PathVariable Long solicitudId,
            @RequestParam(defaultValue = "3") int topK) {
        return ResponseEntity.ok(recomendacionService.calcularMejoresMatches(solicitudId, topK));
    }
}
