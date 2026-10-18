package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.ArchivoAdjuntoDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import bo.edu.uagrm.backend.services.ArchivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/v1/archivos")
@Tag(name = "Archivos Adjuntos", description = "Endpoints para carga, descarga y gestión de evidencias visuales y documentos (HU14)")
public class ArchivoController {

    private final ArchivoService archivoService;
    private final UsuarioRepository usuarioRepository;

    public ArchivoController(ArchivoService archivoService, UsuarioRepository usuarioRepository) {
        this.archivoService = archivoService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping(value = "/publicaciones/{publicacionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Adjuntar archivo a publicación (HU14)", description = "Sube una imagen (JPG, PNG) o PDF de evidencia. Límite máximo: 5MB.")
    public ResponseEntity<ArchivoAdjuntoDTO> adjuntarArchivo(
            @PathVariable Long publicacionId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Usuario usuario = getAuthenticatedUser(authentication);
        boolean admin = esAdmin(authentication);
        ArchivoAdjuntoDTO adjunto = archivoService.adjuntarArchivo(publicacionId, usuario.getId(), admin, file);
        return new ResponseEntity<>(adjunto, HttpStatus.CREATED);
    }

    @GetMapping("/publicaciones/{publicacionId}")
    @Operation(summary = "Listar archivos de una publicación")
    public ResponseEntity<List<ArchivoAdjuntoDTO>> listarArchivosPorPublicacion(@PathVariable Long publicacionId) {
        return ResponseEntity.ok(archivoService.obtenerArchivosPorPublicacion(publicacionId));
    }

    @GetMapping("/{nombreAlmacenado:.+}")
    @Operation(summary = "Descargar o visualizar archivo adjunto")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable String nombreAlmacenado) {
        Resource resource = archivoService.cargarArchivoComoRecurso(nombreAlmacenado);

        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
        } catch (IOException ignored) {}

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{archivoId}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Eliminar archivo adjunto (Autor o Admin)")
    public ResponseEntity<String> eliminarArchivo(@PathVariable Long archivoId, Authentication authentication) {
        Usuario usuario = getAuthenticatedUser(authentication);
        boolean admin = esAdmin(authentication);
        archivoService.eliminarArchivo(archivoId, usuario.getId(), admin);
        return ResponseEntity.ok("Archivo eliminado correctamente");
    }

    private Usuario getAuthenticatedUser(Authentication authentication) {
        String correo = authentication.getName();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }

    private boolean esAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
