package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.CategoriaDTO;
import bo.edu.uagrm.backend.dtos.MensajeResponseDTO;
import bo.edu.uagrm.backend.services.CategoriaService;
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
@RequestMapping({"/categories", "/categorias"})
@Tag(name = "Categorías de Publicación", description = "Endpoints para la gestión de categorías de ayuda (HU7)")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping({"", "/"})
    @Operation(summary = "Listar categorías activas", description = "Retorna el catálogo de categorías activas para selección en formularios")
    public ResponseEntity<List<CategoriaDTO>> listarActivas() {
        return ResponseEntity.ok(categoriaService.obtenerCategoriasActivas());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Listar todas las categorías (Admin)", description = "Retorna todas las categorías, incluyendo inactivas")
    public ResponseEntity<List<CategoriaDTO>> listarTodas() {
        return ResponseEntity.ok(categoriaService.obtenerTodasCategorias());
    }

    @GetMapping({"/{id}"})
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<CategoriaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @PostMapping({"", "/"})
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Crear nueva categoría (Admin)")
    public ResponseEntity<CategoriaDTO> crearCategoria(@Valid @RequestBody CategoriaDTO dto) {
        return new ResponseEntity<>(categoriaService.crearCategoria(dto), HttpStatus.CREATED);
    }

    @PutMapping({"/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Actualizar categoría (Admin)")
    public ResponseEntity<CategoriaDTO> actualizarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaDTO dto) {
        return ResponseEntity.ok(categoriaService.actualizarCategoria(id, dto));
    }

    @DeleteMapping({"/{id}"})
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Eliminar o inactivar categoría (Admin)", description = "Si tiene publicaciones asociadas se inactiva para preservar la integridad; si no, se elimina")
    public ResponseEntity<MensajeResponseDTO> eliminarODesactivar(@PathVariable Long id) {
        String msg = categoriaService.eliminarODesactivarCategoria(id);
        return ResponseEntity.ok(MensajeResponseDTO.of(msg, id, null));
    }

    @PatchMapping({"/{id}/status", "/{id}/estado"})
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Cambiar estado activo/inactivo (Admin)")
    public ResponseEntity<CategoriaDTO> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return ResponseEntity.ok(categoriaService.cambiarEstado(id, activo));
    }
}
