package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.ArchivoAdjuntoDTO;
import bo.edu.uagrm.backend.entities.ArchivoAdjunto;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.ArchivoAdjuntoRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArchivoService {

    private final Path uploadLocation;
    private final ArchivoAdjuntoRepository archivoRepository;
    private final PublicacionRepository publicacionRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB en bytes (HU14 - Criterio 2)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );

    public ArchivoService(@Value("${app.upload.dir:uploads}") String uploadDir,
                          ArchivoAdjuntoRepository archivoRepository,
                          PublicacionRepository publicacionRepository) {
        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.archivoRepository = archivoRepository;
        this.publicacionRepository = publicacionRepository;

        try {
            Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio para almacenamiento de archivos", e);
        }
    }

    @Transactional
    public ArchivoAdjuntoDTO adjuntarArchivo(Long publicacionId, Long usuarioId, boolean esAdmin, MultipartFile file) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada con ID: " + publicacionId));

        // Solo el autor o el admin pueden adjuntar archivos a la publicación
        if (!publicacion.getUsuario().getId().equals(usuarioId) && !esAdmin) {
            throw new BadRequestException("No tiene permisos para adjuntar archivos a esta publicación");
        }

        validarArchivo(file);

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = getExtension(originalFilename).toLowerCase();
        String storedFilename = UUID.randomUUID() + "." + fileExtension;

        try {
            Path targetLocation = this.uploadLocation.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String urlDescarga = "/api/v1/archivos/" + storedFilename;

            ArchivoAdjunto adjunto = ArchivoAdjunto.builder()
                    .publicacion(publicacion)
                    .nombreOriginal(originalFilename)
                    .nombreAlmacenado(storedFilename)
                    .tipoMime(file.getContentType())
                    .tamanoBytes(file.getSize())
                    .urlDescarga(urlDescarga)
                    .build();

            adjunto = archivoRepository.save(adjunto);
            return mapToDTO(adjunto);

        } catch (IOException ex) {
            throw new RuntimeException("Error al almacenar el archivo en disco: " + originalFilename, ex);
        }
    }

    @Transactional(readOnly = true)
    public List<ArchivoAdjuntoDTO> obtenerArchivosPorPublicacion(Long publicacionId) {
        return archivoRepository.findByPublicacionId(publicacionId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Resource cargarArchivoComoRecurso(String nombreAlmacenado) {
        try {
            Path filePath = this.uploadLocation.resolve(nombreAlmacenado).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("El archivo solicitado no existe o no se puede leer");
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Ruta de archivo inválida: " + nombreAlmacenado);
        }
    }

    @Transactional
    public void eliminarArchivo(Long archivoId, Long usuarioId, boolean esAdmin) {
        ArchivoAdjunto adjunto = archivoRepository.findById(archivoId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado con ID: " + archivoId));

        Publicacion publicacion = adjunto.getPublicacion();
        if (!publicacion.getUsuario().getId().equals(usuarioId) && !esAdmin) {
            throw new BadRequestException("No tiene permisos para eliminar este archivo");
        }

        try {
            Path filePath = this.uploadLocation.resolve(adjunto.getNombreAlmacenado()).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {}

        archivoRepository.delete(adjunto);
    }

    private void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo adjunto no puede estar vacío");
        }

        // Criterio 2: Límite máximo de 5MB
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("El archivo excede el tamaño máximo permitido de 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BadRequestException("El nombre del archivo es inválido");
        }

        // Criterio 1: Extensiones permitidas JPG, PNG, PDF
        String extension = getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Formato no permitido (" + extension + "). Solo se permiten archivos JPG, PNG y PDF.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Tipo de contenido no permitido: " + contentType);
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    public ArchivoAdjuntoDTO mapToDTO(ArchivoAdjunto adjunto) {
        boolean esImagen = adjunto.getTipoMime() != null && adjunto.getTipoMime().startsWith("image/");
        return ArchivoAdjuntoDTO.builder()
                .id(adjunto.getId())
                .nombreOriginal(adjunto.getNombreOriginal())
                .nombreAlmacenado(adjunto.getNombreAlmacenado())
                .tipoMime(adjunto.getTipoMime())
                .tamanoBytes(adjunto.getTamanoBytes())
                .urlDescarga(adjunto.getUrlDescarga())
                .esImagen(esImagen)
                .fechaCreacion(adjunto.getFechaCreacion())
                .build();
    }
}
