package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.AuditoriaResponseDTO;
import bo.edu.uagrm.backend.entities.AuditoriaPublicacion;
import bo.edu.uagrm.backend.entities.PerfilUsuario;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.enums.TipoAccionAuditoria;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.repositories.AuditoriaPublicacionRepository;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private final AuditoriaPublicacionRepository auditoriaRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;

    public AuditoriaService(AuditoriaPublicacionRepository auditoriaRepository,
                            PerfilUsuarioRepository perfilUsuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
    }

    @Transactional
    public void registrarEvento(Publicacion publicacion, Usuario responsable, TipoAccionAuditoria tipoAccion,
                                String campoModificado, String valorAnterior, String valorNuevo, String motivo) {
        AuditoriaPublicacion auditoria = AuditoriaPublicacion.builder()
                .publicacion(publicacion)
                .usuario(responsable)
                .tipoAccion(tipoAccion)
                .campoModificado(campoModificado)
                .valorAnterior(valorAnterior)
                .valorNuevo(valorNuevo)
                .motivo(motivo)
                .build();

        auditoriaRepository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaResponseDTO> obtenerHistorialPorPublicacion(Long publicacionId, int page, int size) {
        int pageNumber = Math.max(0, page);
        int pageSize = size > 0 ? Math.min(size, 50) : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return auditoriaRepository.findByPublicacionId(publicacionId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaResponseDTO> obtenerHistorialGeneral(int page, int size) {
        int pageNumber = Math.max(0, page);
        int pageSize = size > 0 ? Math.min(size, 50) : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        return auditoriaRepository.findAllByOrderByFechaRegistroDesc(pageable)
                .map(this::mapToDTO);
    }

    public AuditoriaResponseDTO mapToDTO(AuditoriaPublicacion a) {
        Usuario u = a.getUsuario();
        PerfilUsuario perfil = perfilUsuarioRepository.findById(u.getId()).orElse(null);
        String usuarioNombre = (perfil != null) ? perfil.getNombres() + " " + perfil.getApellidos() : u.getCorreo();

        return AuditoriaResponseDTO.builder()
                .id(a.getId())
                .publicacionId(a.getPublicacion().getId())
                .publicacionTitulo(a.getPublicacion().getTitulo())
                .usuarioId(u.getId())
                .usuarioNombre(usuarioNombre)
                .usuarioCorreo(u.getCorreo())
                .tipoAccion(a.getTipoAccion())
                .campoModificado(a.getCampoModificado())
                .valorAnterior(a.getValorAnterior())
                .valorNuevo(a.getValorNuevo())
                .motivo(a.getMotivo())
                .fechaRegistro(a.getFechaRegistro())
                .build();
    }
}
