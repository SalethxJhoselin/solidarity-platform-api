package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.PublicacionResponseDTO;
import bo.edu.uagrm.backend.dtos.PublicarActividadColectivaRequestDTO;
import bo.edu.uagrm.backend.dtos.PublicarDonacionRequestDTO;
import bo.edu.uagrm.backend.dtos.PublicarNecesidadRequestDTO;
import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.repositories.*;
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
class PublicacionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @Mock
    private CategoriaPublicacionRepository categoriaRepository;

    @Mock
    private TipoRecursoRepository tipoRecursoRepository;

    @Mock
    private DisponibilidadVoluntarioRepository disponibilidadRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private TrazabilidadService trazabilidadService;

    @InjectMocks
    private PublicacionService publicacionService;

    private Usuario usuario;
    private CategoriaPublicacion categoria;
    private TipoRecurso tipoRecurso;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(10L);
        usuario.setCorreo("estudiante@uagrm.edu.bo");

        categoria = CategoriaPublicacion.builder()
                .id(1L)
                .nombre("Tutoría Académica")
                .activo(true)
                .build();

        tipoRecurso = TipoRecurso.builder()
                .id(5L)
                .nombre("Libros Universitarios")
                .unidadMedida("Unidades")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("HU9: Publicar una necesidad de ayuda con estado ABIERTA")
    void publicarNecesidad_Exitoso() {
        PublicarNecesidadRequestDTO request = PublicarNecesidadRequestDTO.builder()
                .titulo("Necesito apoyo para cálculo I")
                .descripcion("Requiero ayuda para entender integrales dobles")
                .categoriaId(1L)
                .build();

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(perfilUsuarioRepository.findById(10L)).thenReturn(Optional.empty());

        Publicacion publicacionGuardada = Publicacion.builder()
                .id(100L)
                .usuario(usuario)
                .tipo(TipoPublicacion.NECESIDAD)
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .categoria(categoria)
                .estado(EstadoPublicacion.ABIERTA)
                .activo(true)
                .build();

        when(publicacionRepository.save(any(Publicacion.class))).thenReturn(publicacionGuardada);

        PublicacionResponseDTO response = publicacionService.publicarNecesidad(10L, request);

        assertNotNull(response);
        assertEquals(TipoPublicacion.NECESIDAD, response.getTipo());
        assertEquals(EstadoPublicacion.ABIERTA, response.getEstado());
        assertTrue(response.getActivo());
    }

    @Test
    @DisplayName("HU12: Criterio 1 - Validar que Fecha Fin no sea anterior a Fecha Inicio")
    void publicarActividadColectiva_FechaFinAnteriorAInicio_LanzaBadRequestException() {
        PublicarActividadColectivaRequestDTO request = PublicarActividadColectivaRequestDTO.builder()
                .titulo("Rifa Solidaria de Invierno")
                .descripcion("Recaudación para estudiantes de provincia")
                .lugar("Módulo 236 FICCT")
                .fechaInicio(LocalDateTime.now().plusDays(5))
                .fechaFin(LocalDateTime.now().plusDays(2)) // Fecha Fin ANTERIOR a Fecha Inicio
                .build();

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                publicacionService.publicarActividadColectiva(10L, request));

        assertTrue(ex.getMessage().contains("no puede ser anterior"));
        verify(publicacionRepository, never()).save(any(Publicacion.class));
    }

    @Test
    @DisplayName("HU11: Publicar donación rechaza tipo de recurso inactivo")
    void publicarDonacion_TipoRecursoInactivo_LanzaBadRequestException() {
        tipoRecurso.setActivo(false); // Inactivo

        PublicarDonacionRequestDTO request = PublicarDonacionRequestDTO.builder()
                .titulo("Donación de libros de física")
                .descripcion("Física Universitaria Sears Zemansky")
                .tipoRecursoId(5L)
                .cantidad(3)
                .build();

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(tipoRecursoRepository.findById(5L)).thenReturn(Optional.of(tipoRecurso));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                publicacionService.publicarDonacion(10L, request));

        assertTrue(ex.getMessage().contains("inactivo"));
        verify(publicacionRepository, never()).save(any(Publicacion.class));
    }

    @Test
    @DisplayName("HU13: Criterio 2 - Eliminar publicación aplica Soft Delete")
    void eliminarPublicacion_SoftDelete_Exitoso() {
        Publicacion publicacion = Publicacion.builder()
                .id(50L)
                .usuario(usuario)
                .tipo(TipoPublicacion.NECESIDAD)
                .titulo("Apoyo transporte")
                .descripcion("Detalle")
                .estado(EstadoPublicacion.ABIERTA)
                .activo(true)
                .build();

        when(publicacionRepository.findById(50L)).thenReturn(Optional.of(publicacion));

        publicacionService.eliminarPublicacionPropia(50L, 10L, false);

        assertFalse(publicacion.getActivo());
        assertEquals(EstadoPublicacion.CANCELADA, publicacion.getEstado());
        verify(publicacionRepository).save(publicacion);
        verify(publicacionRepository, never()).delete(any(Publicacion.class));
    }

    @Test
    @DisplayName("HU13: Criterio 1 - Impedir que un usuario ajeno elimine la publicación de otro")
    void eliminarPublicacion_UsuarioNoAutorNiAdmin_LanzaBadRequestException() {
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(99L); // ID diferente al autor (10L)

        Publicacion publicacion = Publicacion.builder()
                .id(50L)
                .usuario(usuario) // Creador es 10L
                .activo(true)
                .build();

        when(publicacionRepository.findById(50L)).thenReturn(Optional.of(publicacion));

        assertThrows(BadRequestException.class, () ->
                publicacionService.eliminarPublicacionPropia(50L, 99L, false));

        verify(publicacionRepository, never()).save(any(Publicacion.class));
    }
}
