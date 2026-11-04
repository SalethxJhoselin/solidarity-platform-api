package bo.edu.uagrm.backend.mappers;

import bo.edu.uagrm.backend.dtos.BandejaReportesDTO;
import bo.edu.uagrm.backend.dtos.ReporteResponseDTO;
import bo.edu.uagrm.backend.entities.PerfilUsuario;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.ReporteContenido;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.enums.MotivoReporte;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Enterprise mapper responsible for transforming {@link ReporteContenido} entities
 * into {@link ReporteResponseDTO} and {@link BandejaReportesDTO} contracts.
 */
@Component
public class ReporteMapper {

    private final PerfilUsuarioRepository perfilUsuarioRepository;

    public ReporteMapper(PerfilUsuarioRepository perfilUsuarioRepository) {
        this.perfilUsuarioRepository = perfilUsuarioRepository;
    }

    public ReporteResponseDTO toDTO(ReporteContenido r) {
        if (r == null) {
            return null;
        }

        Usuario d = r.getDenunciante();
        PerfilUsuario perfil = (d != null)
                ? perfilUsuarioRepository.findById(d.getId()).orElse(null)
                : null;
        String denuncianteNombre = (perfil != null)
                ? perfil.getNombres() + " " + perfil.getApellidos()
                : (d != null ? d.getCorreo() : "Usuario");

        return ReporteResponseDTO.builder()
                .id(r.getId())
                .denuncianteId(d != null ? d.getId() : null)
                .denuncianteNombre(denuncianteNombre)
                .denuncianteCorreo(d != null ? d.getCorreo() : null)
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

    public BandejaReportesDTO toBandejaDTO(Publicacion pub, List<ReporteContenido> reps) {
        Usuario autor = pub.getUsuario();
        PerfilUsuario perfil = (autor != null)
                ? perfilUsuarioRepository.findById(autor.getId()).orElse(null)
                : null;
        String autorNombre = (perfil != null)
                ? perfil.getNombres() + " " + perfil.getApellidos()
                : (autor != null ? autor.getCorreo() : "Autor");

        List<MotivoReporte> motivos = reps.stream()
                .map(ReporteContenido::getMotivo)
                .distinct()
                .collect(Collectors.toList());

        List<ReporteResponseDTO> detalles = reps.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return BandejaReportesDTO.builder()
                .publicacionId(pub.getId())
                .publicacionTitulo(pub.getTitulo())
                .autorNombre(autorNombre)
                .autorCorreo(autor != null ? autor.getCorreo() : null)
                .tipoPublicacion(pub.getTipo())
                .totalReportes(reps.size())
                .motivos(motivos)
                .fechaUltimoReporte(reps.isEmpty() ? null : reps.get(0).getFechaCreacion())
                .reportesDetalle(detalles)
                .build();
    }
}
