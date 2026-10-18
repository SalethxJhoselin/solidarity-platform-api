package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.SeguimientoSolicitudDTO;
import bo.edu.uagrm.backend.dtos.TimelineHitoDTO;
import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.DonacionConcretadaRepository;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import bo.edu.uagrm.backend.repositories.TrazabilidadSolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrazabilidadServiceTest {

    @Mock
    private TrazabilidadSolicitudRepository trazabilidadRepository;

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private DonacionConcretadaRepository donacionRepository;

    @Mock
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @InjectMocks
    private TrazabilidadService trazabilidadService;

    private Publicacion solicitud;
    private Usuario solicitante;
    private Usuario donante;
    private PerfilUsuario perfilSolicitante;
    private PerfilUsuario perfilDonante;

    @BeforeEach
    void setUp() {
        solicitante = new Usuario();
        solicitante.setId(20L);
        solicitante.setCorreo("solicitante@uagrm.edu.bo");

        perfilSolicitante = PerfilUsuario.builder()
                .usuario(solicitante)
                .nombres("Carlos")
                .apellidos("Mendoza")
                .build();

        donante = new Usuario();
        donante.setId(25L);
        donante.setCorreo("donante@uagrm.edu.bo");

        perfilDonante = PerfilUsuario.builder()
                .usuario(donante)
                .nombres("Laura")
                .apellidos("Roca")
                .build();

        solicitud = Publicacion.builder()
                .id(50L)
                .titulo("Necesito libros de Cálculo I")
                .descripcion("Estudiante de primer semestre requiere libros de Cálculo")
                .usuario(solicitante)
                .estado(EstadoPublicacion.ABIERTA)
                .fechaCreacion(LocalDateTime.now().minusDays(2))
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("HU21: Registrar hito de trazabilidad exitosamente")
    void registrarHito_Exito() {
        trazabilidadService.registrarHito(solicitud, EstadoPublicacion.EN_PROCESO, "Coordinando entrega", donante);
        verify(trazabilidadRepository, times(1)).save(any(TrazabilidadSolicitud.class));
    }

    @Test
    @DisplayName("HU21: Obtener seguimiento y línea de tiempo de solicitud abierta")
    void obtenerSeguimiento_SolicitudAbierta() {
        when(publicacionRepository.findById(50L)).thenReturn(Optional.of(solicitud));
        when(perfilUsuarioRepository.findById(20L)).thenReturn(Optional.of(perfilSolicitante));
        when(trazabilidadRepository.findBySolicitudIdOrderByFechaHitoAsc(50L)).thenReturn(Collections.emptyList());
        when(donacionRepository.findBySolicitudIdOrderByFechaEntregaDesc(50L)).thenReturn(Collections.emptyList());

        SeguimientoSolicitudDTO dto = trazabilidadService.obtenerSeguimiento(50L);

        assertNotNull(dto);
        assertEquals(50L, dto.getSolicitudId());
        assertEquals("Necesito libros de Cálculo I", dto.getTitulo());
        assertEquals(EstadoPublicacion.ABIERTA, dto.getEstadoActual());
        assertEquals("Carlos Mendoza", dto.getSolicitanteNombre());

        List<TimelineHitoDTO> timeline = dto.getLineaDeTiempo();
        assertEquals(3, timeline.size());
        assertTrue(timeline.get(0).isCompletado()); // Hito 1: Creado
        assertFalse(timeline.get(1).isCompletado()); // Hito 2: En Proceso
        assertFalse(timeline.get(2).isCompletado()); // Hito 3: Resuelta
    }

    @Test
    @DisplayName("HU21: Obtener seguimiento de solicitud RESUELTA con donación concretada")
    void obtenerSeguimiento_SolicitudResuelta() {
        solicitud.setEstado(EstadoPublicacion.RESUELTA);

        DonacionConcretada donacion = DonacionConcretada.builder()
                .id(1L)
                .solicitud(solicitud)
                .donante(donante)
                .cantidadEntregada(1)
                .observaciones("Libros entregados en el campus")
                .fechaEntrega(LocalDateTime.now())
                .build();

        when(publicacionRepository.findById(50L)).thenReturn(Optional.of(solicitud));
        when(perfilUsuarioRepository.findById(20L)).thenReturn(Optional.of(perfilSolicitante));
        when(perfilUsuarioRepository.findById(25L)).thenReturn(Optional.of(perfilDonante));
        when(trazabilidadRepository.findBySolicitudIdOrderByFechaHitoAsc(50L)).thenReturn(Collections.emptyList());
        when(donacionRepository.findBySolicitudIdOrderByFechaEntregaDesc(50L)).thenReturn(List.of(donacion));

        SeguimientoSolicitudDTO dto = trazabilidadService.obtenerSeguimiento(50L);

        assertNotNull(dto);
        assertEquals(EstadoPublicacion.RESUELTA, dto.getEstadoActual());
        List<TimelineHitoDTO> timeline = dto.getLineaDeTiempo();
        assertTrue(timeline.get(0).isCompletado()); // Creada
        assertTrue(timeline.get(1).isCompletado()); // En proceso (por haber donación)
        assertTrue(timeline.get(2).isCompletado()); // Resuelta
        assertEquals("Laura Roca", timeline.get(2).getUsuarioResponsable());
    }

    @Test
    @DisplayName("HU21: Lanzar excepción si la solicitud no existe")
    void obtenerSeguimiento_NoExiste() {
        when(publicacionRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> trazabilidadService.obtenerSeguimiento(999L));
    }
}
