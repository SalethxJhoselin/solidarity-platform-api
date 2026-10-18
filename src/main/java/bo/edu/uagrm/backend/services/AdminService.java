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
    private final bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository perfilRepository;

    public AdminService(UsuarioRepository usuarioRepository,
                        RolRepository rolRepository,
                        bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository perfilRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.perfilRepository = perfilRepository;
    }

    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> listUsers() {
        return usuarioRepository.findAll().stream().map(u -> {
            bo.edu.uagrm.backend.entities.PerfilUsuario perfil = perfilRepository.findById(u.getId()).orElse(null);
            String name = u.getCorreo();
            if (perfil != null && perfil.getNombres() != null) {
                name = perfil.getNombres() + (perfil.getApellidos() != null ? " " + perfil.getApellidos() : "");
            }
            java.util.List<String> roles = u.getRoles().stream()
                    .map(Rol::getNombre)
                    .map(r -> r.replace("ROLE_", ""))
                    .map(r -> r.equals("ADMIN") ? "ADMINISTRADOR" : r)
                    .collect(java.util.stream.Collectors.toList());
            return java.util.Map.<String, Object>of(
                    "id", String.valueOf(u.getId()),
                    "name", name.trim(),
                    "email", u.getCorreo(),
                    "roles", roles
            );
        }).collect(java.util.stream.Collectors.toList());
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
