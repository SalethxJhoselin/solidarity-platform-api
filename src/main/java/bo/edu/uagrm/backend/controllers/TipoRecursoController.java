package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.TipoRecursoDTO;
import bo.edu.uagrm.backend.services.TipoRecursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipos-recurso")
@Tag(name = "Tipos de Recurso", description = "Endpoints para la gestión de tipos de recursos donables (HU8)")
public class TipoRecursoController {

    private final TipoRecursoService tipoRecursoService;

    public TipoRecursoController(TipoRecursoService tipoRecursoService) {
        this.tipoRecursoService = tipoRecursoService;
    }

    @GetMapping
    @Operation(summary = "Listar tipos de recursos activos", description = "Retorna el catálogo de tipos de recursos disponibles para donaciones")
    public ResponseEntity<List<TipoRecursoDTO>> listarActivos() {
        return ResponseEntity.ok(tipoRecursoService.obtenerTiposActivos());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Listar todos los tipos de recurso (Admin)", description = "Retorna todos los tipos, incluyendo inactivos")
    public ResponseEntity<List<TipoRecursoDTO>> listarTodos() {
        return ResponseEntity.ok(tipoRecursoService.obtenerTodosTipos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de recurso por ID")
    public ResponseEntity<TipoRecursoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tipoRecursoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Crear nuevo tipo de recurso (Admin)")
    public ResponseEntity<TipoRecursoDTO> crearTipoRecurso(@Valid @RequestBody TipoRecursoDTO dto) {
        return new ResponseEntity<>(tipoRecursoService.crearTipoRecurso(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Actualizar tipo de recurso (Admin)")
    public ResponseEntity<TipoRecursoDTO> actualizarTipoRecurso(@PathVariable Long id, @Valid @RequestBody TipoRecursoDTO dto) {
        return ResponseEntity.ok(tipoRecursoService.actualizarTipoRecurso(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Eliminar o inactivar tipo de recurso (Admin)", description = "Si tiene donaciones registradas se inactiva; si no, se elimina")
    public ResponseEntity<String> eliminarODesactivar(@PathVariable Long id) {
        return ResponseEntity.ok(tipoRecursoService.eliminarODesactivarTipoRecurso(id));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Cambiar estado activo/inactivo (Admin)")
    public ResponseEntity<TipoRecursoDTO> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return ResponseEntity.ok(tipoRecursoService.cambiarEstado(id, activo));
    }
}
