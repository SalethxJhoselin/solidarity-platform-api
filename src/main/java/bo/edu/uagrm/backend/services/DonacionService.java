package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.DonacionConcretadaRequestDTO;
import bo.edu.uagrm.backend.dtos.DonacionConcretadaResponseDTO;
import bo.edu.uagrm.backend.entities.DonacionConcretada;
import bo.edu.uagrm.backend.entities.EstadoPublicacion;
import bo.edu.uagrm.backend.entities.PerfilUsuario;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.DonacionConcretadaRepository;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonacionService {

    private final DonacionConcretadaRepository donacionRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final TrazabilidadService trazabilidadService;
    private final AuditoriaService auditoriaService;

    public DonacionService(DonacionConcretadaRepository donacionRepository,
                           PublicacionRepository publicacionRepository,
                           UsuarioRepository usuarioRepository,
                           PerfilUsuarioRepository perfilUsuarioRepository,
                           TrazabilidadService trazabilidadService,
                           AuditoriaService auditoriaService) {
        this.donacionRepository = donacionRepository;
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
        this.trazabilidadService = trazabilidadService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public DonacionConcretadaResponseDTO registrarEntregaAyuda(Long donanteId, DonacionConcretadaRequestDTO request) {
        Usuario donante = usuarioRepository.findById(donanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario donante/voluntario no encontrado con ID: " + donanteId));

        Publicacion solicitud = publicacionRepository.findById(request.getSolicitudId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de ayuda no encontrada con ID: " + request.getSolicitudId()));

        if (!solicitud.getActivo()) {
            throw new BadRequestException("La solicitud seleccionada se encuentra cancelada o inactiva");
        }

        if (solicitud.getEstado() == EstadoPublicacion.RESUELTA) {
            throw new BadRequestException("Esta solicitud de ayuda ya fue marcada como RESUELTA anteriormente");
        }

        // Criterio 1 y 2: Enlazar donante con solicitud y generar registro inmutable de trazabilidad
        DonacionConcretada donacion = DonacionConcretada.builder()
                .solicitud(solicitud)
                .donante(donante)
                .cantidadEntregada(request.getCantidadEntregada() != null ? request.getCantidadEntregada() : 1)
                .observaciones(request.getObservaciones())
                .build();

        donacion = donacionRepository.save(donacion);

        // Criterio 3: Cambiar automáticamente el estado de la solicitud a "RESUELTA"
        solicitud.setEstado(EstadoPublicacion.RESUELTA);
        publicacionRepository.save(solicitud);

        // HU20 & HU21: Registrar en auditoría e hito en la línea de tiempo de trazabilidad
        trazabilidadService.registrarHito(solicitud, EstadoPublicacion.RESUELTA,
                "Donación/Ayuda completada satisfactoriamente por donante", donante);
        auditoriaService.registrarEvento(solicitud, donante, bo.edu.uagrm.backend.entities.TipoAccionAuditoria.CAMBIO_ESTADO,
                "estado", EstadoPublicacion.ABIERTA.name(), EstadoPublicacion.RESUELTA.name(),
                "Solicitud de ayuda resuelta tras entrega de donación");

        return mapToDTO(donacion);
    }

    @Transactional(readOnly = true)
    public List<DonacionConcretadaResponseDTO> listarEntregasPorSolicitud(Long solicitudId) {
        return donacionRepository.findBySolicitudIdOrderByFechaEntregaDesc(solicitudId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DonacionConcretadaResponseDTO> listarMisDonacionesEntregadas(Long donanteId) {
        return donacionRepository.findByDonanteIdOrderByFechaEntregaDesc(donanteId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DonacionConcretadaResponseDTO mapToDTO(DonacionConcretada d) {
        Publicacion s = d.getSolicitud();
        Usuario solUsuario = s.getUsuario();
        PerfilUsuario solPerfil = perfilUsuarioRepository.findById(solUsuario.getId()).orElse(null);
        String solicitanteNombre = (solPerfil != null) ? solPerfil.getNombres() + " " + solPerfil.getApellidos() : solUsuario.getCorreo();

        Usuario donUsuario = d.getDonante();
        PerfilUsuario donPerfil = perfilUsuarioRepository.findById(donUsuario.getId()).orElse(null);
        String donanteNombre = (donPerfil != null) ? donPerfil.getNombres() + " " + donPerfil.getApellidos() : donUsuario.getCorreo();

        return DonacionConcretadaResponseDTO.builder()
                .id(d.getId())
                .solicitudId(s.getId())
                .solicitudTitulo(s.getTitulo())
                .solicitanteNombre(solicitanteNombre)
                .solicitanteCorreo(solUsuario.getCorreo())
                .donanteId(donUsuario.getId())
                .donanteNombre(donanteNombre)
                .donanteCorreo(donUsuario.getCorreo())
                .cantidadEntregada(d.getCantidadEntregada())
                .observaciones(d.getObservaciones())
                .fechaEntrega(d.getFechaEntrega())
                .build();
    }
}
