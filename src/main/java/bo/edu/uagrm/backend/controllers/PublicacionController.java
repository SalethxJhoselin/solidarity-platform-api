package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.*;
import bo.edu.uagrm.backend.entities.TipoPublicacion;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import bo.edu.uagrm.backend.services.PublicacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/publicaciones")
@Tag(name = "Publicaciones", description = "Endpoints para Necesidades, Voluntariado, Donaciones y Actividades Colectivas (HU9 a HU13)")
public class PublicacionController {

    private final PublicacionService publicacionService;
    private final UsuarioRepository usuarioRepository;

    public PublicacionController(PublicacionService publicacionService, UsuarioRepository usuarioRepository) {
        this.publicacionService = publicacionService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/necesidad")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Publicar necesidad de ayuda (HU9)", description = "Permite a un estudiante registrar una nueva necesidad clasificada por categoría")
    public ResponseEntity<PublicacionResponseDTO> publicarNecesidad(
            Authentication authentication,
            @Valid @RequestBody PublicarNecesidadRequestDTO request) {
        Usuario usuario = getAuthenticatedUser(authentication);
        return new ResponseEntity<>(publicacionService.publicarNecesidad(usuario.getId(), request), HttpStatus.CREATED);
    }

    @PostMapping("/voluntariado")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Publicar oferta de voluntariado (HU10)", description = "Permite a un voluntario publicar su disponibilidad horaria y áreas de especialización")
    public ResponseEntity<PublicacionResponseDTO> publicarVoluntariado(
            Authentication authentication,
            @Valid @RequestBody PublicarVoluntariadoRequestDTO request) {
        Usuario usuario = getAuthenticatedUser(authentication);
        return new ResponseEntity<>(publicacionService.publicarVoluntariado(usuario.getId(), request), HttpStatus.CREATED);
    }

    @PostMapping("/donacion")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Publicar oferta de donación (HU11)", description = "Permite a un donante registrar bienes físicos disponibles basados en el catálogo de recursos")
    public ResponseEntity<PublicacionResponseDTO> publicarDonacion(
            Authentication authentication,
            @Valid @RequestBody PublicarDonacionRequestDTO request) {
        Usuario usuario = getAuthenticatedUser(authentication);
        return new ResponseEntity<>(publicacionService.publicarDonacion(usuario.getId(), request), HttpStatus.CREATED);
    }

    @PostMapping("/actividad-colectiva")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Publicar actividad colectiva (HU12)", description = "Permite registrar rifas, kermesses o campañas solidarias con fechas y ubicación")
    public ResponseEntity<PublicacionResponseDTO> publicarActividadColectiva(
            Authentication authentication,
            @Valid @RequestBody PublicarActividadColectivaRequestDTO request) {
        Usuario usuario = getAuthenticatedUser(authentication);
        return new ResponseEntity<>(publicacionService.publicarActividadColectiva(usuario.getId(), request), HttpStatus.CREATED);
    }

    @GetMapping("/mis-publicaciones")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Consultar publicaciones propias (HU13)", description = "Retorna el listado de publicaciones creadas por el usuario autenticado")
    public ResponseEntity<List<PublicacionResponseDTO>> obtenerMisPublicaciones(Authentication authentication) {
        Usuario usuario = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(publicacionService.obtenerMisPublicaciones(usuario.getId()));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar y filtrar publicaciones (HU16)", description = "Permite buscar por texto (case-insensitive) y filtrar por tipo, categoría, tipo de recurso y antigüedad con paginación")
    public ResponseEntity<org.springframework.data.domain.Page<PublicacionResponseDTO>> buscarPublicaciones(
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

    @GetMapping
    @Operation(summary = "Listar publicaciones activas", description = "Retorna el tablón de publicaciones activas. Opcionalmente filtrable por tipo: NECESIDAD, VOLUNTARIADO, DONACION, ACTIVIDAD_COLECTIVA")
    public ResponseEntity<List<PublicacionResponseDTO>> listarPublicaciones(
            @RequestParam(required = false) TipoPublicacion tipo) {
        return ResponseEntity.ok(publicacionService.listarPublicacionesActivas(tipo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una publicación")
    public ResponseEntity<PublicacionResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(publicacionService.obtenerPublicacionPorId(id));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Editar publicación propia (HU13)", description = "Permite al autor o al administrador modificar el contenido y detalles de su publicación")
    public ResponseEntity<PublicacionResponseDTO> actualizarPublicacion(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody ActualizarPublicacionRequestDTO request) {
        Usuario usuario = getAuthenticatedUser(authentication);
        boolean admin = esAdmin(authentication);
        return ResponseEntity.ok(publicacionService.actualizarPublicacionPropia(id, usuario.getId(), admin, request));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Eliminar / dar de baja publicación propia (HU13 - Soft Delete)", description = "Aplica borrado lógico cambiando el estado a CANCELADA y activo a false")
    public ResponseEntity<String> eliminarPublicacion(
            @PathVariable Long id,
            Authentication authentication) {
        Usuario usuario = getAuthenticatedUser(authentication);
        boolean admin = esAdmin(authentication);
        publicacionService.eliminarPublicacionPropia(id, usuario.getId(), admin);
        return ResponseEntity.ok("Publicación dada de baja correctamente (Soft Delete aplicado)");
    }

    private Usuario getAuthenticatedUser(Authentication authentication) {
        String correo = authentication.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado con correo: " + correo));
    }

    private boolean esAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
