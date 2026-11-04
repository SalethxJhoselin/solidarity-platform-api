package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.DonacionConcretadaRequestDTO;
import bo.edu.uagrm.backend.dtos.DonacionConcretadaResponseDTO;
import bo.edu.uagrm.backend.entities.DonacionConcretada;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.enums.EstadoPublicacion;
import bo.edu.uagrm.backend.enums.TipoAccionAuditoria;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.mappers.DonacionMapper;
import bo.edu.uagrm.backend.repositories.DonacionConcretadaRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Enterprise service responsible for managing confirmed donations and help delivery (HU17).
 */
@Slf4j
@Service
public class DonacionService {

    private final DonacionConcretadaRepository donacionRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final DonacionMapper donacionMapper;
    private final TrazabilidadService trazabilidadService;
    private final AuditoriaService auditoriaService;

    public DonacionService(DonacionConcretadaRepository donacionRepository,
                           PublicacionRepository publicacionRepository,
                           UsuarioRepository usuarioRepository,
                           DonacionMapper donacionMapper,
                           TrazabilidadService trazabilidadService,
                           AuditoriaService auditoriaService) {
        this.donacionRepository = donacionRepository;
        this.publicacionRepository = publicacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.donacionMapper = donacionMapper;
        this.trazabilidadService = trazabilidadService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public DonacionConcretadaResponseDTO registrarEntregaAyuda(Long donanteId, DonacionConcretadaRequestDTO request) {
        Usuario donante = usuarioRepository.findById(donanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario donante/voluntario no encontrado con ID: " + donanteId));

        Publicacion solicitud = publicacionRepository.findById(request.getSolicitudId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de ayuda no encontrada con ID: " + request.getSolicitudId()));

        if (!Boolean.TRUE.equals(solicitud.getActivo())) {
            throw new BadRequestException("La solicitud seleccionada se encuentra cancelada o inactiva");
        }

        if (solicitud.getEstado() == EstadoPublicacion.RESUELTA) {
            throw new BadRequestException("La solicitud ya fue completamente resuelta con anterioridad");
        }

        if (solicitud.getUsuario().getId().equals(donanteId)) {
            throw new BadRequestException("El autor de la solicitud no puede registrar una auto-donación en su propia publicación");
        }

        int cantidad = (request.getCantidadEntregada() != null && request.getCantidadEntregada() > 0)
                ? request.getCantidadEntregada()
                : (request.getCantidad() != null && request.getCantidad() > 0 ? request.getCantidad() : 1);

        boolean quedoResuelta = true;
        if (solicitud.getCantidadDisponible() != null && solicitud.getCantidadDisponible() > 0) {
            int restante = Math.max(0, solicitud.getCantidadDisponible() - cantidad);
            solicitud.setCantidadDisponible(restante);
            if (restante > 0) {
                quedoResuelta = false;
                solicitud.setEstado(EstadoPublicacion.EN_PROCESO);
            } else {
                solicitud.setEstado(EstadoPublicacion.RESUELTA);
            }
        } else {
            solicitud.setEstado(EstadoPublicacion.RESUELTA);
        }

        publicacionRepository.save(solicitud);

        DonacionConcretada donacion = DonacionConcretada.builder()
                .solicitud(solicitud)
                .donante(donante)
                .cantidadEntregada(cantidad)
                .observaciones(request.getObservaciones() != null ? request.getObservaciones().trim() : null)
                .build();

        donacion = donacionRepository.save(donacion);
        log.info("Donation {} confirmed for request {} by donor {}. Request resolved: {}",
                donacion.getId(), solicitud.getId(), donanteId, quedoResuelta);

        // HU20 & HU21: Audit trail and traceability timeline registration
        EstadoPublicacion nuevoEstado = solicitud.getEstado();
        trazabilidadService.registrarHito(solicitud, nuevoEstado,
                quedoResuelta ? "Donación/Ayuda completada satisfactoriamente (Solicitud RESUELTA)"
                              : "Entrega parcial registrada (" + cantidad + " unidades entregadas. Restan: " + solicitud.getCantidadDisponible() + ")",
                donante);
        auditoriaService.registrarEvento(solicitud, donante, TipoAccionAuditoria.CAMBIO_ESTADO,
                "estado", EstadoPublicacion.ABIERTA.name(), nuevoEstado.name(),
                "Aporte/Donación registrada en la publicación");

        return donacionMapper.toDTO(donacion);
    }

    @Transactional(readOnly = true)
    public List<DonacionConcretadaResponseDTO> listarEntregasPorSolicitud(Long solicitudId) {
        return donacionRepository.findBySolicitudIdOrderByFechaEntregaDesc(solicitudId).stream()
                .map(donacionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DonacionConcretadaResponseDTO> listarMisDonacionesEntregadas(Long donanteId) {
        return donacionRepository.findByDonanteIdOrderByFechaEntregaDesc(donanteId).stream()
                .map(donacionMapper::toDTO)
                .collect(Collectors.toList());
    }
}
