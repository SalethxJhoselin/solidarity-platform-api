package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.ReporteRequestDTO;
import bo.edu.uagrm.backend.dtos.ReporteResponseDTO;
import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final ReporteContenidoRepository reporteRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final AuditoriaService auditoriaService;

    public ReporteService(ReporteContenidoRepository reporteRepository,
                          PublicacionRepository publicacionRepository,
                          ComentarioRepository comentarioRepository,
                          UsuarioRepository usuarioRepository,
                          PerfilUsuarioRepository perfilUsuarioRepository,
                          AuditoriaService auditoriaService) {
        this.reporteRepository = reporteRepository;
        this.publicacionRepository = publicacionRepository;
        this.comentarioRepository = comentarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
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

            if (reporteRepository.existsByDenuncianteIdAndPublicacionId(denuncianteId, request.getPublicacionId())) {
                throw new BadRequestException("Ya has enviado un reporte previamente para esta publicación");
            }
        }

        Comentario comentario = null;
        if (request.getComentarioId() != null) {
            comentario = comentarioRepository.findById(request.getComentarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comentario reportado no encontrado"));

            if (reporteRepository.existsByDenuncianteIdAndComentarioId(denuncianteId, request.getComentarioId())) {
                throw new BadRequestException("Ya has enviado un reporte previamente para este comentario");
            }
        }

        ReporteContenido reporte = ReporteContenido.builder()
                .denunciante(denunciante)
                .publicacion(publicacion)
                .comentario(comentario)
                .motivo(request.getMotivo())
                .detalles(request.getDetalles())
                .estadoReporte(EstadoReporte.PENDIENTE)
                .build();

        reporte = reporteRepository.save(reporte);
        return mapToDTO(reporte);
    }

    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> listarReportes(EstadoReporte estado) {
        List<ReporteContenido> lista;
        if (estado != null) {
            lista = reporteRepository.findByEstadoReporteOrderByFechaCreacionDesc(estado);
        } else {
            lista = reporteRepository.findAllByOrderByFechaCreacionDesc();
        }

        return lista.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public ReporteResponseDTO resolverReporte(Long reporteId, EstadoReporte nuevoEstado, String resolucionAdmin) {
        ReporteContenido reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado con ID: " + reporteId));

        reporte.setEstadoReporte(nuevoEstado);
        reporte.setResolucionAdmin(resolucionAdmin);
        reporte.setFechaResolucion(LocalDateTime.now());

        // Si se sanciona/aprueba el reporte, se puede bloquear la publicación reportada
        if (nuevoEstado == EstadoReporte.APROBADO_SANCIONADO && reporte.getPublicacion() != null) {
            Publicacion pub = reporte.getPublicacion();
            pub.setEstado(EstadoPublicacion.BLOQUEADA);
            pub.setActivo(false);
            publicacionRepository.save(pub);
        }

        reporte = reporteRepository.save(reporte);
        return mapToDTO(reporte);
    }

    // ==========================================
    // HU19: Bandeja de entrada acumulada y moderación Admin
    // ==========================================
    @Transactional(readOnly = true)
    public List<bo.edu.uagrm.backend.dtos.BandejaReportesDTO> obtenerBandejaReportesAcumulados() {
        List<ReporteContenido> reportesPendientes = reporteRepository.findByEstadoReporteOrderByFechaCreacionDesc(EstadoReporte.PENDIENTE);

        Map<Long, List<ReporteContenido>> porPublicacion = reportesPendientes.stream()
                .filter(r -> r.getPublicacion() != null)
                .collect(Collectors.groupingBy(r -> r.getPublicacion().getId()));

        List<bo.edu.uagrm.backend.dtos.BandejaReportesDTO> bandeja = new java.util.ArrayList<>();

        for (Map.Entry<Long, List<ReporteContenido>> entry : porPublicacion.entrySet()) {
            List<ReporteContenido> reps = entry.getValue();
            Publicacion pub = reps.get(0).getPublicacion();
            Usuario autor = pub.getUsuario();
            PerfilUsuario perfil = perfilUsuarioRepository.findById(autor.getId()).orElse(null);
            String autorNombre = (perfil != null) ? perfil.getNombres() + " " + perfil.getApellidos() : autor.getCorreo();

            List<MotivoReporte> motivos = reps.stream().map(ReporteContenido::getMotivo).distinct().collect(Collectors.toList());
            List<ReporteResponseDTO> detalles = reps.stream().map(this::mapToDTO).collect(Collectors.toList());

            bandeja.add(bo.edu.uagrm.backend.dtos.BandejaReportesDTO.builder()
                    .publicacionId(pub.getId())
                    .publicacionTitulo(pub.getTitulo())
                    .autorNombre(autorNombre)
                    .autorCorreo(autor.getCorreo())
                    .tipoPublicacion(pub.getTipo())
                    .totalReportes(reps.size())
                    .motivos(motivos)
                    .fechaUltimoReporte(reps.get(0).getFechaCreacion())
                    .reportesDetalle(detalles)
                    .build());
        }

        bandeja.sort((a, b) -> Integer.compare(b.getTotalReportes(), a.getTotalReportes()));
        return bandeja;
    }

    @Transactional
    public void bloquearPublicacionPorReportes(Long publicacionId, String resolucionAdmin) {
        Publicacion pub = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación no encontrada"));

        pub.setEstado(EstadoPublicacion.BLOQUEADA);
        pub.setActivo(false);
        publicacionRepository.save(pub);

        auditoriaService.registrarEvento(pub, null, bo.edu.uagrm.backend.entities.TipoAccionAuditoria.BLOQUEO_ADMIN,
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
    }

    public ReporteResponseDTO mapToDTO(ReporteContenido r) {
        Usuario d = r.getDenunciante();
        PerfilUsuario perfil = perfilUsuarioRepository.findById(d.getId()).orElse(null);
        String denuncianteNombre = (perfil != null) ? perfil.getNombres() + " " + perfil.getApellidos() : d.getCorreo();

        return ReporteResponseDTO.builder()
                .id(r.getId())
                .denuncianteId(d.getId())
                .denuncianteNombre(denuncianteNombre)
                .denuncianteCorreo(d.getCorreo())
                .publicacionId(r.getPublicacion() != null ? r.getPublicacion().getId() : null)
                .publicacionTitulo(r.getPublicacion() != null ? r.getPublicacion().getTitulo() : null)
                .comentarioId(r.getComentario() != null ? r.getComentario().getId() : null)
                .comentarioTexto(r.getComentario() != null ? r.getComentario().getContenido() : null)
                .motivo(r.getMotivo())
                .detalles(r.getDetalles())
                .estadoReporte(r.getEstadoReporte())
                .resolucionAdmin(r.getResolucionAdmin())
                .fechaCreacion(r.getFechaCreacion())
                .fechaResolucion(r.getFechaResolucion())
                .build();
    }
}
