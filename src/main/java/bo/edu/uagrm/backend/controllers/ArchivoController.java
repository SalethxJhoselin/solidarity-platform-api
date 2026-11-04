package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.ArchivoAdjuntoDTO;
import bo.edu.uagrm.backend.dtos.MensajeResponseDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.security.CurrentUser;
import bo.edu.uagrm.backend.services.ArchivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * REST controller for file upload, download, and attachment management (HU14).
 */
@RestController
@RequestMapping({"/files", "/archivos"})
@Tag(name = "Archivos Adjuntos", description = "Endpoints for evidence upload, document download, and file attachments (HU14)")
public class ArchivoController {

    private final ArchivoService archivoService;

    public ArchivoController(ArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    @PostMapping(value = {"/publications/{publicacionId}", "/publicaciones/{publicacionId}"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Attach file to publication (HU14)", description = "Uploads an evidence image (JPG, PNG) or PDF document. Max size: 5MB.")
    public ResponseEntity<ArchivoAdjuntoDTO> adjuntarArchivo(
            @PathVariable Long publicacionId,
            @RequestParam("file") MultipartFile file,
            @CurrentUser Usuario usuario) {
        ArchivoAdjuntoDTO adjunto = archivoService.adjuntarArchivo(publicacionId, usuario.getId(), usuario.esAdmin(), file);
        return new ResponseEntity<>(adjunto, HttpStatus.CREATED);
    }

    @GetMapping({"/publications/{publicacionId}", "/publicaciones/{publicacionId}"})
    @Operation(summary = "List attachments for a publication")
    public ResponseEntity<List<ArchivoAdjuntoDTO>> listarArchivosPorPublicacion(@PathVariable Long publicacionId) {
        return ResponseEntity.ok(archivoService.obtenerArchivosPorPublicacion(publicacionId));
    }

    @GetMapping("/{nombreAlmacenado:.+}")
    @Operation(summary = "Download or stream attachment resource")
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
    @Operation(summary = "Delete attachment (Author or Admin)")
    public ResponseEntity<MensajeResponseDTO> eliminarArchivo(
            @PathVariable Long archivoId,
            @CurrentUser Usuario usuario) {
        archivoService.eliminarArchivo(archivoId, usuario.getId(), usuario.esAdmin());
        return ResponseEntity.ok(MensajeResponseDTO.of("Archivo eliminado correctamente", archivoId, null));
    }
}
