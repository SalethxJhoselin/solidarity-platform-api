package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.ReporteRequestDTO;
import bo.edu.uagrm.backend.dtos.ReporteResponseDTO;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteContenidoRepository reporteRepository;

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ReporteService reporteService;

    private Usuario denunciante;
    private Publicacion publicacion;

    @BeforeEach
    void setUp() {
        denunciante = new Usuario();
        denunciante.setId(30L);
        denunciante.setCorreo("denunciante@uagrm.edu.bo");

        publicacion = Publicacion.builder()
                .id(80L)
                .titulo("Venta de rifa falsa")
                .build();
    }

    @Test
    @DisplayName("HU18: Criterio 2 - Crear reporte genera alerta en estado PENDIENTE")
    void crearReporte_Exitoso() {
        ReporteRequestDTO request = ReporteRequestDTO.builder()
                .publicacionId(80L)
                .motivo(MotivoReporte.INFORMACION_FALSA_O_FRAUDE)
                .detalles("El usuario solicita depósitos a cuenta personal sospechosa")
                .build();

        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(denunciante));
        when(publicacionRepository.findById(80L)).thenReturn(Optional.of(publicacion));
        when(reporteRepository.existsByDenuncianteIdAndPublicacionId(30L, 80L)).thenReturn(false);
        when(perfilUsuarioRepository.findById(30L)).thenReturn(Optional.empty());

        when(reporteRepository.save(any(ReporteContenido.class))).thenAnswer(inv -> {
            ReporteContenido r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ReporteResponseDTO response = reporteService.crearReporte(30L, request);

        assertNotNull(response);
        assertEquals(MotivoReporte.INFORMACION_FALSA_O_FRAUDE, response.getMotivo());
        assertEquals(EstadoReporte.PENDIENTE, response.getEstadoReporte());
        verify(reporteRepository).save(any(ReporteContenido.class));
    }

    @Test
    @DisplayName("HU18: Impedir que el mismo usuario envíe reporte duplicado sobre la misma publicación")
    void crearReporte_Duplicado_LanzaBadRequestException() {
        ReporteRequestDTO request = ReporteRequestDTO.builder()
                .publicacionId(80L)
                .motivo(MotivoReporte.SPAM_O_PUBLICIDAD)
                .build();

        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(denunciante));
        when(publicacionRepository.findById(80L)).thenReturn(Optional.of(publicacion));
        when(reporteRepository.existsByDenuncianteIdAndPublicacionId(30L, 80L)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                reporteService.crearReporte(30L, request));

        assertTrue(ex.getMessage().contains("Ya has enviado un reporte previamente"));
        verify(reporteRepository, never()).save(any(ReporteContenido.class));
    }

    @Test
    @DisplayName("HU19: Obtener bandeja de reportes acumulados agrupados por publicación")
    void obtenerBandejaReportesAcumulados_Exito() {
        Usuario autor = new Usuario();
        autor.setId(5L);
        autor.setCorreo("autor@uagrm.edu.bo");
        publicacion.setUsuario(autor);
        publicacion.setTipo(TipoPublicacion.NECESIDAD);

        ReporteContenido rep1 = ReporteContenido.builder()
                .id(1L)
                .publicacion(publicacion)
                .denunciante(denunciante)
                .motivo(MotivoReporte.INFORMACION_FALSA_O_FRAUDE)
                .estadoReporte(EstadoReporte.PENDIENTE)
                .build();

        ReporteContenido rep2 = ReporteContenido.builder()
                .id(2L)
                .publicacion(publicacion)
                .denunciante(denunciante)
                .motivo(MotivoReporte.CONTENIDO_OFENSIVO)
                .estadoReporte(EstadoReporte.PENDIENTE)
                .build();

        when(reporteRepository.findByEstadoReporteOrderByFechaCreacionDesc(EstadoReporte.PENDIENTE))
                .thenReturn(List.of(rep1, rep2));
        when(perfilUsuarioRepository.findById(5L)).thenReturn(Optional.empty());
        when(perfilUsuarioRepository.findById(30L)).thenReturn(Optional.empty());

        var bandeja = reporteService.obtenerBandejaReportesAcumulados();

        assertNotNull(bandeja);
        assertEquals(1, bandeja.size());
        assertEquals(80L, bandeja.get(0).getPublicacionId());
        assertEquals(2, bandeja.get(0).getTotalReportes());
        assertEquals(2, bandeja.get(0).getMotivos().size());
    }

    @Test
    @DisplayName("HU19: Acción de 1 clic para dar de baja/bloquear publicación por reportes")
    void bloquearPublicacionPorReportes_Exito() {
        ReporteContenido rep = ReporteContenido.builder()
                .id(1L)
                .publicacion(publicacion)
                .denunciante(denunciante)
                .estadoReporte(EstadoReporte.PENDIENTE)
                .build();

        when(publicacionRepository.findById(80L)).thenReturn(Optional.of(publicacion));
        when(reporteRepository.findAll()).thenReturn(List.of(rep));

        reporteService.bloquearPublicacionPorReportes(80L, "Incumplimiento grave de normas");

        assertEquals(EstadoPublicacion.BLOQUEADA, publicacion.getEstado());
        assertFalse(publicacion.getActivo());
        assertEquals(EstadoReporte.APROBADO_SANCIONADO, rep.getEstadoReporte());
        verify(publicacionRepository).save(publicacion);
        verify(reporteRepository).save(rep);
        verify(auditoriaService).registrarEvento(eq(publicacion), isNull(), eq(TipoAccionAuditoria.BLOQUEO_ADMIN),
                eq("estado"), eq("ABIERTA"), eq("BLOQUEADA"), anyString());
    }

    @Test
    @DisplayName("HU19: Acción de 1 clic para descartar reportes de una publicación")
    void descartarReportesDePublicacion_Exito() {
        ReporteContenido rep = ReporteContenido.builder()
                .id(1L)
                .publicacion(publicacion)
                .denunciante(denunciante)
                .estadoReporte(EstadoReporte.PENDIENTE)
                .build();

        when(reporteRepository.findAll()).thenReturn(List.of(rep));

        reporteService.descartarReportesDePublicacion(80L, "Reporte sin fundamentos válidos");

        assertEquals(EstadoReporte.DESCARTADO, rep.getEstadoReporte());
        assertEquals("Reporte sin fundamentos válidos", rep.getResolucionAdmin());
        verify(reporteRepository).save(rep);
    }
}
