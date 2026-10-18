package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.MatchRecomendacionDTO;
import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchRecomendacionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @InjectMocks
    private MatchRecomendacionService matchService;

    private Publicacion solicitud;
    private Usuario solicitante;
    private PerfilUsuario perfilSol;

    private Publicacion ofertaDonacion;
    private Usuario donante;
    private PerfilUsuario perfilDon;

    private CategoriaPublicacion categoria;
    private TipoRecurso recurso;

    @BeforeEach
    void setUp() {
        categoria = CategoriaPublicacion.builder()
                .id(1L)
                .nombre("Material Académico")
                .build();

        recurso = TipoRecurso.builder()
                .id(1L)
                .nombre("Libros y Textos")
                .build();

        solicitante = new Usuario();
        solicitante.setId(10L);
        solicitante.setCorreo("estudiante@uagrm.edu.bo");

        perfilSol = PerfilUsuario.builder()
                .usuario(solicitante)
                .nombres("Juan")
                .apellidos("Perez")
                .facultad("Facultad de Ingeniería en Ciencias de la Computación")
                .carrera("Ingeniería Informática")
                .build();

        solicitud = Publicacion.builder()
                .id(100L)
                .titulo("Necesito libros de Programación en Java")
                .descripcion("Requiero apoyo con libros de Java y algoritmos para el semestre")
                .categoria(categoria)
                .usuario(solicitante)
                .tipo(TipoPublicacion.NECESIDAD)
                .estado(EstadoPublicacion.ABIERTA)
                .activo(true)
                .build();

        donante = new Usuario();
        donante.setId(20L);
        donante.setCorreo("donante@uagrm.edu.bo");

        perfilDon = PerfilUsuario.builder()
                .usuario(donante)
                .nombres("Pedro")
                .apellidos("Gomez")
                .facultad("Facultad de Ingeniería en Ciencias de la Computación")
                .carrera("Ingeniería Informática")
                .telefono("70011223")
                .build();

        ofertaDonacion = Publicacion.builder()
                .id(200L)
                .titulo("Dono libros de Java y Estructuras de Datos")
                .descripcion("Tengo libros de programación en Java listos para donar")
                .categoria(categoria)
                .tipoRecurso(recurso)
                .cantidadDisponible(2)
                .usuario(donante)
                .tipo(TipoPublicacion.DONACION)
                .estado(EstadoPublicacion.ABIERTA)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("HU22: Calcular mejores matches con alta afinidad (Categoría + Facultad + Carrera + Recurso + Semántica)")
    void calcularMejoresMatches_AltaAfinidad() {
        when(publicacionRepository.findById(100L)).thenReturn(Optional.of(solicitud));
        when(perfilUsuarioRepository.findById(10L)).thenReturn(Optional.of(perfilSol));

        when(publicacionRepository.findByTipoAndEstadoAndActivoTrueOrderByFechaCreacionDesc(
                TipoPublicacion.VOLUNTARIADO, EstadoPublicacion.ABIERTA))
                .thenReturn(Collections.emptyList());

        when(publicacionRepository.findByTipoAndEstadoAndActivoTrueOrderByFechaCreacionDesc(
                TipoPublicacion.DONACION, EstadoPublicacion.ABIERTA))
                .thenReturn(List.of(ofertaDonacion));

        when(perfilUsuarioRepository.findById(20L)).thenReturn(Optional.of(perfilDon));

        List<MatchRecomendacionDTO> matches = matchService.calcularMejoresMatches(100L, 3);

        assertNotNull(matches);
        assertEquals(1, matches.size());
        MatchRecomendacionDTO match = matches.get(0);
        assertEquals(200L, match.getPublicacionId());
        assertEquals("Pedro Gomez", match.getNombreOferente());
        assertEquals("70011223", match.getTelefono());
        assertTrue(match.getPorcentajeCompatibilidad() >= 80.0, "El porcentaje de compatibilidad debe ser alto (> 80%)");
        assertTrue(match.getEtiquetaCompatibilidad().startsWith("Match "));
        assertFalse(match.getFactoresCoincidencia().isEmpty());
    }

    @Test
    @DisplayName("HU22: Excluir al mismo usuario de sus propias recomendaciones de match")
    void calcularMejoresMatches_ExcluyeMismoUsuario() {
        // Oferta del mismo solicitante
        ofertaDonacion.setUsuario(solicitante);

        when(publicacionRepository.findById(100L)).thenReturn(Optional.of(solicitud));
        when(perfilUsuarioRepository.findById(10L)).thenReturn(Optional.of(perfilSol));
        when(publicacionRepository.findByTipoAndEstadoAndActivoTrueOrderByFechaCreacionDesc(
                TipoPublicacion.VOLUNTARIADO, EstadoPublicacion.ABIERTA))
                .thenReturn(Collections.emptyList());
        when(publicacionRepository.findByTipoAndEstadoAndActivoTrueOrderByFechaCreacionDesc(
                TipoPublicacion.DONACION, EstadoPublicacion.ABIERTA))
                .thenReturn(List.of(ofertaDonacion));

        List<MatchRecomendacionDTO> matches = matchService.calcularMejoresMatches(100L, 3);

        assertNotNull(matches);
        assertTrue(matches.isEmpty(), "No debe incluir ofertas creadas por el mismo usuario");
    }
}
