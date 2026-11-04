package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.DonacionConcretadaRequestDTO;
import bo.edu.uagrm.backend.dtos.DonacionConcretadaResponseDTO;
import bo.edu.uagrm.backend.entities.DonacionConcretada;
import bo.edu.uagrm.backend.enums.EstadoPublicacion;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.repositories.DonacionConcretadaRepository;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonacionServiceTest {

    @Mock
    private DonacionConcretadaRepository donacionRepository;

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @Mock
    private TrazabilidadService trazabilidadService;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private DonacionService donacionService;

    private Usuario donante;
    private Usuario solicitante;
    private Publicacion solicitud;

    @BeforeEach
    void setUp() {
        donante = new Usuario();
        donante.setId(20L);
        donante.setCorreo("donante@uagrm.edu.bo");

        solicitante = new Usuario();
        solicitante.setId(10L);
        solicitante.setCorreo("solicitante@uagrm.edu.bo");

        solicitud = Publicacion.builder()
                .id(100L)
                .usuario(solicitante)
                .titulo("Solicitud de libro de cálculo")
                .estado(EstadoPublicacion.ABIERTA)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("HU17: Criterio 3 - Registrar entrega actualiza automáticamente la solicitud a RESUELTA")
    void registrarEntregaAyuda_Exitoso_CambiaEstadoAResuelta() {
        DonacionConcretadaRequestDTO request = DonacionConcretadaRequestDTO.builder()
                .solicitudId(100L)
                .cantidadEntregada(1)
                .observaciones("Libro entregado en biblioteca central de la FICCT")
                .build();

        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(donante));
        when(publicacionRepository.findById(100L)).thenReturn(Optional.of(solicitud));
        when(perfilUsuarioRepository.findById(any())).thenReturn(Optional.empty());

        when(donacionRepository.save(any(DonacionConcretada.class))).thenAnswer(invocation -> {
            DonacionConcretada d = invocation.getArgument(0);
            d.setId(1L);
            return d;
        });

        DonacionConcretadaResponseDTO response = donacionService.registrarEntregaAyuda(20L, request);

        assertNotNull(response);
        assertEquals(EstadoPublicacion.RESUELTA, solicitud.getEstado());
        verify(publicacionRepository).save(solicitud);
        verify(donacionRepository).save(any(DonacionConcretada.class));
    }

    @Test
    @DisplayName("HU17: Impedir registrar entrega sobre solicitud ya resuelta")
    void registrarEntregaAyuda_YaResuelta_LanzaBadRequestException() {
        solicitud.setEstado(EstadoPublicacion.RESUELTA); // Ya está resuelta

        DonacionConcretadaRequestDTO request = DonacionConcretadaRequestDTO.builder()
                .solicitudId(100L)
                .build();

        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(donante));
        when(publicacionRepository.findById(100L)).thenReturn(Optional.of(solicitud));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                donacionService.registrarEntregaAyuda(20L, request));

        assertTrue(ex.getMessage().contains("ya fue marcada como RESUELTA"));
        verify(donacionRepository, never()).save(any(DonacionConcretada.class));
    }
}
