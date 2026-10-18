package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.CategoriaDTO;
import bo.edu.uagrm.backend.entities.CategoriaPublicacion;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.repositories.CategoriaPublicacionRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaPublicacionRepository categoriaRepository;

    @Mock
    private PublicacionRepository publicacionRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private CategoriaPublicacion categoria;
    private CategoriaDTO categoriaDTO;

    @BeforeEach
    void setUp() {
        categoria = CategoriaPublicacion.builder()
                .id(1L)
                .nombre("Apoyo Económico")
                .descripcion("Ayuda para estudiantes")
                .icono("FaMoneyBillWave")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        categoriaDTO = CategoriaDTO.builder()
                .nombre("Apoyo Económico")
                .descripcion("Ayuda para estudiantes")
                .icono("FaMoneyBillWave")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("HU7: Crear categoría exitosamente cuando el nombre es único")
    void crearCategoria_Exitoso() {
        when(categoriaRepository.existsByNombreIgnoreCase("Apoyo Económico")).thenReturn(false);
        when(categoriaRepository.save(any(CategoriaPublicacion.class))).thenReturn(categoria);

        CategoriaDTO result = categoriaService.crearCategoria(categoriaDTO);

        assertNotNull(result);
        assertEquals("Apoyo Económico", result.getNombre());
        verify(categoriaRepository).save(any(CategoriaPublicacion.class));
    }

    @Test
    @DisplayName("HU7: Impedir creación de categoría si ya existe el nombre (case-insensitive)")
    void crearCategoria_NombreDuplicado_LanzaBadRequestException() {
        when(categoriaRepository.existsByNombreIgnoreCase("Apoyo Económico")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoriaService.crearCategoria(categoriaDTO));
        verify(categoriaRepository, never()).save(any(CategoriaPublicacion.class));
    }

    @Test
    @DisplayName("HU7: Criterio 2 - Si la categoría tiene publicaciones vinculadas, se desactiva en vez de borrar")
    void eliminarODesactivar_ConPublicaciones_CambiaAInactiva() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(publicacionRepository.existsByCategoriaId(1L)).thenReturn(true);

        String mensaje = categoriaService.eliminarODesactivarCategoria(1L);

        assertTrue(mensaje.contains("Inactiva"));
        assertFalse(categoria.getActivo());
        verify(categoriaRepository).save(categoria);
        verify(categoriaRepository, never()).delete(any(CategoriaPublicacion.class));
    }

    @Test
    @DisplayName("HU7: Si la categoría no tiene publicaciones vinculadas, se elimina físicamente")
    void eliminarODesactivar_SinPublicaciones_EliminaFisicamente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(publicacionRepository.existsByCategoriaId(1L)).thenReturn(false);

        String mensaje = categoriaService.eliminarODesactivarCategoria(1L);

        assertTrue(mensaje.contains("eliminada permanentemente"));
        verify(categoriaRepository).delete(categoria);
    }
}
