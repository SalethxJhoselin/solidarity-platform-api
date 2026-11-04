package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.BandejaReportesDTO;
import bo.edu.uagrm.backend.dtos.ReporteRequestDTO;
import bo.edu.uagrm.backend.dtos.ReporteResponseDTO;
import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.enums.EstadoPublicacion;
import bo.edu.uagrm.backend.enums.EstadoReporte;
import bo.edu.uagrm.backend.enums.TipoAccionAuditoria;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.mappers.ReporteMapper;
import bo.edu.uagrm.backend.repositories.ComentarioRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import bo.edu.uagrm.backend.repositories.ReporteContenidoRepository;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enterprise service managing community moderation reports and administrative triage (HU18, HU19).
 */
@Slf4j
@Service
public class ReporteService {

    private final ReporteContenidoRepository reporteRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReporteMapper reporteMapper;
    private final AuditoriaService auditoriaService;

    public ReporteService(ReporteContenidoRepository reporteRepository,
                          PublicacionRepository publicacionRepository,
                          ComentarioRepository comentarioRepository,
                          UsuarioRepository usuarioRepository,
                          ReporteMapper reporteMapper,
                          AuditoriaService auditoriaService) {
        this.reporteRepository = reporteRepository;
        this.publicacionRepository = publicacionRepository;
        this.comentarioRepository = comentarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.reporteMapper = reporteMapper;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ReporteResponseDTO crearReporte(Long denuncianteId, ReporteRequestDTO request) {
        Usuario denunciante = usuarioRepository.findById(denuncianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario denunciante no encontrado"));

        if (request.getPublicacionId() == null && request.getComentarioId() == null) {
            throw new BadRequestException("Debe especificar una publicación o un comentario para realizar el reporte");
        }

        Publicacion publicacion = null;
        if (request.getPublicacionId() != null) {
            publicacion = publicacionRepository.findById(request.getPublicacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Publicación reportada no encontrada"));
        }

        Comentario comentario = null;
        if (request.getComentarioId() != null) {
            comentario = comentarioRepository.findById(request.getComentarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comentario reportado no encontrado"));
            if (publicacion == null) {
                publicacion = comentario.getPublicacion();
            }
        }

        ReporteContenido reporte = ReporteContenido.builder()
                .denunciante(denunciante)
                .publicacion(publicacion)
                .comentario(comentario)
                .motivo(request.getMotivo())
                .detalles(request.getDetalles() != null ? request.getDetalles().trim() : null)
                .estadoReporte(EstadoReporte.PENDIENTE)
                .build();

        reporte = reporteRepository.save(reporte);
        log.info("Report {} submitted by user {} against publication {}", reporte.getId(), denuncianteId,
                publicacion != null ? publicacion.getId() : "N/A");
        return reporteMapper.toDTO(reporte);
    }

    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> listarReportes(EstadoReporte estado) {
        List<ReporteContenido> reportes = (estado != null)
                ? reporteRepository.findByEstadoReporteOrderByFechaCreacionDesc(estado)
                : reporteRepository.findAllByOrderByFechaCreacionDesc();

        return reportes.stream()
                .map(reporteMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BandejaReportesDTO> obtenerBandejaReportesAcumulados() {
        List<ReporteContenido> reportesPendientes = reporteRepository.findByEstadoReporteOrderByFechaCreacionDesc(EstadoReporte.PENDIENTE);

        Map<Long, List<ReporteContenido>> porPublicacion = reportesPendientes.stream()
                .filter(r -> r.getPublicacion() != null)
                .collect(Collectors.groupingBy(r -> r.getPublicacion().getId()));

        List<BandejaReportesDTO> bandeja = new ArrayList<>();

        for (Map.Entry<Long, List<ReporteContenido>> entry : porPublicacion.entrySet()) {
            List<ReporteContenido> reps = entry.getValue();
            Publicacion pub = reps.get(0).getPublicacion();
            bandeja.add(reporteMapper.toBandejaDTO(pub, reps));
        }

        bandeja.sort((a, b) -> Integer.compare(b.getTotalReportes(), a.getTotalReportes()));
        return bandeja;
    }

    @Transactional
    public void bloquearPublicacionPorReportes(Long publicacionId, String resolucionAdmin) {
        bloquearPublicacionPorReportes(publicacionId, null, resolucionAdmin);
    }

    @Transactional
    public void bloquearPublicacionPorReportes(Long publicacionId, Usuario admin, String resolucionAdmin) {
        Publicacion pub = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        pub.setEstado(EstadoPublicacion.BLOQUEADA);
        pub.setActivo(false);
        publicacionRepository.save(pub);

        auditoriaService.registrarEvento(pub, admin, TipoAccionAuditoria.BLOQUEO_ADMIN,
                "estado", "ABIERTA", "BLOQUEADA", "Bloqueo administrativo por acumulación de reportes comunitarios: " + resolucionAdmin);

        List<ReporteContenido> reportes = reporteRepository.findAll().stream()
                .filter(r -> r.getPublicacion() != null && r.getPublicacion().getId().equals(publicacionId)
                        && r.getEstadoReporte() == EstadoReporte.PENDIENTE)
                .collect(Collectors.toList());

        for (ReporteContenido r : reportes) {
            r.setEstadoReporte(EstadoReporte.APROBADO_SANCIONADO);
            r.setResolucionAdmin(resolucionAdmin != null ? resolucionAdmin : "Publicación bloqueada por incumplimiento de normas comunitarias");
            r.setFechaResolucion(LocalDateTime.now());
            reporteRepository.save(r);
        }

        log.warn("Publication {} blocked by admin {} with {} pending reports resolved",
                publicacionId, admin != null ? admin.getCorreo() : "SYSTEM", reportes.size());
    }

    @Transactional
    public void descartarReportesDePublicacion(Long publicacionId, String motivoDescarte) {
        List<ReporteContenido> reportes = reporteRepository.findAll().stream()
                .filter(r -> r.getPublicacion() != null && r.getPublicacion().getId().equals(publicacionId)
                        && r.getEstadoReporte() == EstadoReporte.PENDIENTE)
                .collect(Collectors.toList());

        for (ReporteContenido r : reportes) {
            r.setEstadoReporte(EstadoReporte.DESCARTADO);
            r.setResolucionAdmin(motivoDescarte != null ? motivoDescarte : "Reporte descartado por considerarse no infractor");
            r.setFechaResolucion(LocalDateTime.now());
            reporteRepository.save(r);
        }
        log.info("Dismissed {} reports for publication {}", reportes.size(), publicacionId);
    }

    @Transactional
    public ReporteResponseDTO resolverReporte(Long reporteId, EstadoReporte nuevoEstado, String resolucionAdmin) {
        ReporteContenido reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

        reporte.setEstadoReporte(nuevoEstado);
        reporte.setResolucionAdmin(resolucionAdmin);
        reporte.setFechaResolucion(LocalDateTime.now());
        reporte = reporteRepository.save(reporte);
        log.info("Report {} resolved to state {}", reporteId, nuevoEstado);
        return reporteMapper.toDTO(reporte);
    }
}
