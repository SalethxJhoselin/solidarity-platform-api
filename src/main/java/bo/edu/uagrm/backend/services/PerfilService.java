package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.PerfilDTO;
import bo.edu.uagrm.backend.entities.PerfilUsuario;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

    private final PerfilUsuarioRepository perfilUsuarioRepository;

    public PerfilService(PerfilUsuarioRepository perfilUsuarioRepository) {
        this.perfilUsuarioRepository = perfilUsuarioRepository;
    }

    public PerfilDTO getPerfil(Long usuarioId) {
        PerfilUsuario perfil = perfilUsuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado para el usuario con ID: " + usuarioId));

        return PerfilDTO.builder()
                .nombres(perfil.getNombres())
                .apellidos(perfil.getApellidos())
                .descripcion(perfil.getDescripcion())
                .facultad(perfil.getFacultad())
                .carrera(perfil.getCarrera())
                .urlFotoPerfil(perfil.getUrlFotoPerfil())
                .telefono(perfil.getTelefono())
                .build();
    }

    @Transactional
    public PerfilDTO updatePerfil(Long usuarioId, PerfilDTO perfilDTO) {
        PerfilUsuario perfil = perfilUsuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado para el usuario con ID: " + usuarioId));

        perfil.setNombres(perfilDTO.getNombres());
        perfil.setApellidos(perfilDTO.getApellidos());
        perfil.setDescripcion(perfilDTO.getDescripcion());
        perfil.setFacultad(perfilDTO.getFacultad());
        perfil.setCarrera(perfilDTO.getCarrera());
        perfil.setUrlFotoPerfil(perfilDTO.getUrlFotoPerfil());
        perfil.setTelefono(perfilDTO.getTelefono());

        perfilUsuarioRepository.save(perfil);

        return perfilDTO;
    }
}
