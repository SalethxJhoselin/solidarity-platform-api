package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.RoleAssignRequestDTO;
import bo.edu.uagrm.backend.entities.Rol;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.RolRepository;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public AdminService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Transactional
    public void assignRoles(RoleAssignRequestDTO requestDTO) {
        Usuario usuario = usuarioRepository.findById(requestDTO.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Set<Rol> rolesAsignar = new HashSet<>();
        for (String originalRolNombre : requestDTO.getRoles()) {
            // Asegurarnos que el rol empieza con ROLE_
            final String finalRolNombre;
            if (!originalRolNombre.startsWith("ROLE_")) {
                finalRolNombre = "ROLE_" + originalRolNombre.toUpperCase();
            } else {
                finalRolNombre = originalRolNombre.toUpperCase();
            }
            
            Rol rol = rolRepository.findByNombre(finalRolNombre)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + finalRolNombre));
            rolesAsignar.add(rol);
        }

        usuario.setRoles(rolesAsignar);
        usuarioRepository.save(usuario);
    }
}
