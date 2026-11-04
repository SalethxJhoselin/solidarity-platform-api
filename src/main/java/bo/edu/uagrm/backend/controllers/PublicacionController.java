package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.*;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.enums.TipoPublicacion;
import bo.edu.uagrm.backend.security.CurrentUser;
import bo.edu.uagrm.backend.services.ComentarioService;
import bo.edu.uagrm.backend.services.PublicacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing solidarity publications (Needs, Volunteering, Donations, and Collective Activities).
 */
@RestController
@RequestMapping({"/publications", "/publicaciones"})
@Tag(name = "Publications", description = "Endpoints for Help Requests, Volunteering, Donations, and Collective Campaigns (HU9 to HU13)")
public class PublicacionController {

    private final PublicacionService publicacionService;
    private final ComentarioService comentarioService;

    public PublicacionController(PublicacionService publicacionService,
                                 ComentarioService comentarioService) {
        this.publicacionService = publicacionService;
        this.comentarioService = comentarioService;
    }

    @PostMapping({"/need", "/necesidad"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Publish help request (HU9)", description = "Allows a registered student to publish a categorized help request")
    public ResponseEntity<PublicacionResponseDTO> publicarNecesidad(
            @CurrentUser Usuario usuario,
            @Valid @RequestBody PublicarNecesidadRequestDTO request) {
        return new ResponseEntity<>(publicacionService.publicarNecesidad(usuario.getId(), request), HttpStatus.CREATED);
    }

    @PostMapping({"/volunteering", "/voluntariado"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Publish volunteering offer (HU10)", description = "Allows volunteers to publish their availability schedule and specialization areas")
    public ResponseEntity<PublicacionResponseDTO> publicarVoluntariado(
            @CurrentUser Usuario usuario,
            @Valid @RequestBody PublicarVoluntariadoRequestDTO request) {
        return new ResponseEntity<>(publicacionService.publicarVoluntariado(usuario.getId(), request), HttpStatus.CREATED);
    }

    @PostMapping({"/donation", "/donacion"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Publish donation offer (HU11)", description = "Allows donors to register physical items and goods based on catalogued resources")
    public ResponseEntity<PublicacionResponseDTO> publicarDonacion(
            @CurrentUser Usuario usuario,
            @Valid @RequestBody PublicarDonacionRequestDTO request) {
        return new ResponseEntity<>(publicacionService.publicarDonacion(usuario.getId(), request), HttpStatus.CREATED);
    }

    @PostMapping({"/collective-activity", "/actividad-colectiva"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Publish collective activity (HU12)", description = "Allows publishing community raffles, fairs, and fundraising campaigns with date and location")
    public ResponseEntity<PublicacionResponseDTO> publicarActividadColectiva(
            @CurrentUser Usuario usuario,
            @Valid @RequestBody PublicarActividadColectivaRequestDTO request) {
        return new ResponseEntity<>(publicacionService.publicarActividadColectiva(usuario.getId(), request), HttpStatus.CREATED);
    }

    @GetMapping({"/my-publications", "/mis-publicaciones"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get user's own publications (HU13)", description = "Returns publications created by the authenticated user")
    public ResponseEntity<List<PublicacionResponseDTO>> obtenerMisPublicaciones(@CurrentUser Usuario usuario) {
        return ResponseEntity.ok(publicacionService.obtenerMisPublicaciones(usuario.getId()));
    }

    @GetMapping({"/search", "/buscar"})
    @Operation(summary = "Search and filter publications (HU16)", description = "Case-insensitive text search and multi-criteria filtering by category, resource type, and creation date")
    public ResponseEntity<Page<PublicacionResponseDTO>> buscarPublicaciones(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(required = false) TipoPublicacion tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long tipoRecursoId,
            @RequestParam(defaultValue = "recientes") String orden,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String effectiveQuery = (query != null && !query.isBlank()) ? query : q;
        FiltroPublicacionDTO filtro = FiltroPublicacionDTO.builder()
                .query(effectiveQuery)
                .tipo(tipo)
                .categoriaId(categoriaId)
                .tipoRecursoId(tipoRecursoId)
                .orden(orden)
                .page(page)
                .size(size)
                .build();
        return ResponseEntity.ok(publicacionService.buscarYFiltrar(filtro));
    }

    @GetMapping({"", "/"})
    @Operation(summary = "List active publications", description = "Retrieves active community publications, optionally filtered by publication type")
    public ResponseEntity<List<PublicacionResponseDTO>> listarPublicaciones(
            @RequestParam(required = false) TipoPublicacion tipo) {
        return ResponseEntity.ok(publicacionService.listarPublicacionesActivas(tipo));
    }

    @GetMapping({"/{id}"})
    @Operation(summary = "Get publication details by ID")
    public ResponseEntity<PublicacionResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(publicacionService.obtenerPublicacionPorId(id));
    }

    @GetMapping({"/{id}/comments", "/{id}/comentarios"})
    @Operation(summary = "List comments for a specific publication")
    public ResponseEntity<List<ComentarioResponseDTO>> listarComentariosPorPublicacion(@PathVariable Long id) {
        return ResponseEntity.ok(comentarioService.obtenerComentariosPorPublicacion(id));
    }

    @PutMapping({"/{id}"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update publication (HU13)", description = "Allows author or administrator to update publication content and details")
    public ResponseEntity<PublicacionResponseDTO> actualizarPublicacion(
            @PathVariable Long id,
            @CurrentUser Usuario usuario,
            @Valid @RequestBody ActualizarPublicacionRequestDTO request) {
        return ResponseEntity.ok(publicacionService.actualizarPublicacionPropia(id, usuario.getId(), usuario.esAdmin(), request));
    }

    @DeleteMapping({"/{id}"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Deactivate publication (HU13 - Soft Delete)", description = "Applies soft-delete by changing status to CANCELADA and active flag to false")
    public ResponseEntity<MensajeResponseDTO> eliminarPublicacion(
            @PathVariable Long id,
            @CurrentUser Usuario usuario) {
        publicacionService.eliminarPublicacionPropia(id, usuario.getId(), usuario.esAdmin());
        return ResponseEntity.ok(MensajeResponseDTO.of("Publicación dada de baja correctamente (Soft Delete aplicado)", id, null));
    }
}
