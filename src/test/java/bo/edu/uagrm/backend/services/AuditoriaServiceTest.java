package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.AuditoriaResponseDTO;
import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.repositories.AuditoriaPublicacionRepository;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaPublicacionRepository auditoriaRepository;

    @Mock
    private PerfilUsuarioRepository perfilUsuarioRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    private Publicacion publicacion;
    private Usuario usuario;
    private PerfilUsuario perfil;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo("admin@uagrm.edu.bo");

        perfil = PerfilUsuario.builder()
                .usuario(usuario)
                .nombres("Admin")
                .apellidos("UAGRM")
                .build();

        publicacion = Publicacion.builder()
                .id(100L)
                .titulo("Solicitud de Calculadora Científica")
                .usuario(usuario)
                .estado(EstadoPublicacion.ABIERTA)
                .build();
    }

    @Test
    @DisplayName("HU20: Registrar evento de auditoría exitosamente")
    void registrarEvento_Exito() {
        auditoriaService.registrarEvento(publicacion, usuario, TipoAccionAuditoria.EDICION_TEXTO,
                "titulo", "Titulo anterior", "Nuevo Titulo", "Corrección de ortografía");

        verify(auditoriaRepository, times(1)).save(any(AuditoriaPublicacion.class));
    }

    @Test
    @DisplayName("HU20: Consultar historial de modificaciones por publicación paginado")
    void obtenerHistorialPorPublicacion_Exito() {
        AuditoriaPublicacion auditoria = AuditoriaPublicacion.builder()
                .id(1L)
                .publicacion(publicacion)
                .usuario(usuario)
                .tipoAccion(TipoAccionAuditoria.CAMBIO_ESTADO)
                .campoModificado("estado")
                .valorAnterior("ABIERTA")
                .valorNuevo("RESUELTA")
                .motivo("Donación entregada")
                .fechaRegistro(LocalDateTime.now())
                .build();

        Page<AuditoriaPublicacion> pagina = new PageImpl<>(List.of(auditoria));

        when(auditoriaRepository.findByPublicacionId(eq(100L), any(Pageable.class))).thenReturn(pagina);
        when(perfilUsuarioRepository.findById(1L)).thenReturn(Optional.of(perfil));

        Page<AuditoriaResponseDTO> resultado = auditoriaService.obtenerHistorialPorPublicacion(100L, 0, 10);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        AuditoriaResponseDTO dto = resultado.getContent().get(0);
        assertEquals(100L, dto.getPublicacionId());
        assertEquals("estado", dto.getCampoModificado());
        assertEquals("ABIERTA", dto.getValorAnterior());
        assertEquals("RESUELTA", dto.getValorNuevo());
        assertEquals("Admin UAGRM", dto.getUsuarioNombre());
    }
}
