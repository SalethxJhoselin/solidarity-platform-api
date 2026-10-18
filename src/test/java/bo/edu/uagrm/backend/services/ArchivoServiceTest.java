package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.ArchivoAdjuntoDTO;
import bo.edu.uagrm.backend.entities.ArchivoAdjunto;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.repositories.ArchivoAdjuntoRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchivoServiceTest {

    @Mock
    private ArchivoAdjuntoRepository archivoRepository;

    @Mock
    private PublicacionRepository publicacionRepository;

    @TempDir
    Path tempDir;

    private ArchivoService archivoService;
    private Publicacion publicacion;
    private Usuario autor;

    @BeforeEach
    void setUp() {
        archivoService = new ArchivoService(tempDir.toString(), archivoRepository, publicacionRepository);

        autor = new Usuario();
        autor.setId(10L);

        publicacion = Publicacion.builder()
                .id(1L)
                .usuario(autor)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("HU14: Adjuntar archivo JPG válido de 1MB exitosamente")
    void adjuntarArchivo_Exitoso() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "receta_medica.jpg",
                "image/jpeg",
                new byte[1024 * 1024] // 1MB
        );

        when(publicacionRepository.findById(1L)).thenReturn(Optional.of(publicacion));
        when(archivoRepository.save(any(ArchivoAdjunto.class))).thenAnswer(invocation -> {
            ArchivoAdjunto a = invocation.getArgument(0);
            a.setId(100L);
            return a;
        });

        ArchivoAdjuntoDTO result = archivoService.adjuntarArchivo(1L, 10L, false, file);

        assertNotNull(result);
        assertEquals("receta_medica.jpg", result.getNombreOriginal());
        assertTrue(result.isEsImagen());
        verify(archivoRepository).save(any(ArchivoAdjunto.class));
    }

    @Test
    @DisplayName("HU14: Criterio 2 - Rechazar archivo que exceda el límite de 5MB")
    void adjuntarArchivo_Excede5MB_LanzaBadRequestException() {
        MockMultipartFile filePesado = new MockMultipartFile(
                "file",
                "manual_gigante.pdf",
                "application/pdf",
                new byte[6 * 1024 * 1024] // 6MB (Excede 5MB)
        );

        when(publicacionRepository.findById(1L)).thenReturn(Optional.of(publicacion));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                archivoService.adjuntarArchivo(1L, 10L, false, filePesado));

        assertTrue(ex.getMessage().contains("5MB"));
        verify(archivoRepository, never()).save(any(ArchivoAdjunto.class));
    }

    @Test
    @DisplayName("HU14: Criterio 1 - Rechazar archivo con extensión no permitida (ej. .exe o .zip)")
    void adjuntarArchivo_ExtensionInvalida_LanzaBadRequestException() {
        MockMultipartFile fileInvalido = new MockMultipartFile(
                "file",
                "script_malicioso.exe",
                "application/octet-stream",
                new byte[1024]
        );

        when(publicacionRepository.findById(1L)).thenReturn(Optional.of(publicacion));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                archivoService.adjuntarArchivo(1L, 10L, false, fileInvalido));

        assertTrue(ex.getMessage().contains("Solo se permiten archivos JPG, PNG y PDF"));
        verify(archivoRepository, never()).save(any(ArchivoAdjunto.class));
    }
}
