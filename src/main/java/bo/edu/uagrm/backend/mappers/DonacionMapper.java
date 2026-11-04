package bo.edu.uagrm.backend.mappers;

import bo.edu.uagrm.backend.dtos.DonacionConcretadaResponseDTO;
import bo.edu.uagrm.backend.entities.DonacionConcretada;
import bo.edu.uagrm.backend.entities.PerfilUsuario;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import org.springframework.stereotype.Component;

/**
 * Enterprise mapper responsible for transforming {@link DonacionConcretada} entities
 * into {@link DonacionConcretadaResponseDTO} contracts.
 */
@Component
public class DonacionMapper {

    private final PerfilUsuarioRepository perfilUsuarioRepository;

    public DonacionMapper(PerfilUsuarioRepository perfilUsuarioRepository) {
        this.perfilUsuarioRepository = perfilUsuarioRepository;
    }

    public DonacionConcretadaResponseDTO toDTO(DonacionConcretada d) {
        if (d == null) {
            return null;
        }

        Publicacion s = d.getSolicitud();
        Usuario solUsuario = (s != null) ? s.getUsuario() : null;
        PerfilUsuario solPerfil = (solUsuario != null)
                ? perfilUsuarioRepository.findById(solUsuario.getId()).orElse(null)
                : null;
        String solicitanteNombre = (solPerfil != null)
                ? solPerfil.getNombres() + " " + solPerfil.getApellidos()
                : (solUsuario != null ? solUsuario.getCorreo() : "Solicitante");

        Usuario donUsuario = d.getDonante();
        PerfilUsuario donPerfil = (donUsuario != null)
                ? perfilUsuarioRepository.findById(donUsuario.getId()).orElse(null)
                : null;
        String donanteNombre = (donPerfil != null)
                ? donPerfil.getNombres() + " " + donPerfil.getApellidos()
                : (donUsuario != null ? donUsuario.getCorreo() : "Donante");

        return DonacionConcretadaResponseDTO.builder()
                .id(d.getId())
                .solicitudId(s != null ? s.getId() : null)
                .solicitudTitulo(s != null ? s.getTitulo() : null)
                .solicitanteNombre(solicitanteNombre)
                .solicitanteCorreo(solUsuario != null ? solUsuario.getCorreo() : null)
                .donanteId(donUsuario != null ? donUsuario.getId() : null)
                .donanteNombre(donanteNombre)
                .donanteCorreo(donUsuario != null ? donUsuario.getCorreo() : null)
                .cantidadEntregada(d.getCantidadEntregada())
                .observaciones(d.getObservaciones())
                .fechaEntrega(d.getFechaEntrega())
                .build();
    }
}

