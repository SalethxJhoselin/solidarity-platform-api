package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.enums.*;

import bo.edu.uagrm.backend.dtos.RoleAssignRequestDTO;
import bo.edu.uagrm.backend.dtos.UsuarioAdminDTO;
import bo.edu.uagrm.backend.dtos.ValidacionAdminDTO;
import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import bo.edu.uagrm.backend.repositories.RolRepository;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import bo.edu.uagrm.backend.repositories.ValidacionAcademicaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Enterprise service handling system administration, role assignments, and academic verifications (HU4, HU5, HU6).
 */
@Slf4j
@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PerfilUsuarioRepository perfilRepository;
    private final ValidacionAcademicaRepository validacionRepository;

    public AdminService(UsuarioRepository usuarioRepository,
                        RolRepository rolRepository,
                        PerfilUsuarioRepository perfilRepository,
                        ValidacionAcademicaRepository validacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.perfilRepository = perfilRepository;
        this.validacionRepository = validacionRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminDTO> listUsers() {
        return usuarioRepository.findAll().stream().map(u -> {
            PerfilUsuario perfil = perfilRepository.findById(u.getId()).orElse(null);
            ValidacionAcademica validacion = validacionRepository.findFirstByUsuarioOrderByIdDesc(u).orElse(null);
            return UsuarioAdminDTO.from(u, perfil, validacion);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void cambiarEstadoUsuario(Long usuarioId, EstadoUsuario nuevoEstado) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        // Criterio de Aceptación 3 de HU6: No permitir desactivar la cuenta del Administrador general principal
        if ((nuevoEstado == EstadoUsuario.SUSPENDIDO || nuevoEstado == EstadoUsuario.INACTIVO)
                && "admin@uagrm.edu.bo".equalsIgnoreCase(usuario.getCorreo())) {
            throw new BadRequestException(
                    "No se permite suspender la cuenta del Administrador general principal del sistema.");
        }

        usuario.setEstado(nuevoEstado);
        usuarioRepository.save(usuario);
        log.info("User {} account status transitioned to {}", usuarioId, nuevoEstado);
    }

    @Transactional(readOnly = true)
    public List<ValidacionAdminDTO> listValidaciones() {
        return validacionRepository.findAllByOrderByFechaCreacionDesc().stream().map(v -> {
            PerfilUsuario perfil = perfilRepository.findById(v.getUsuario().getId()).orElse(null);
            return ValidacionAdminDTO.from(v, perfil);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void cambiarEstadoValidacion(Long validacionId, EstadoValidacion nuevoEstado, String adminEmail) {
        ValidacionAcademica validacion = validacionRepository.findById(validacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Validación no encontrada con ID: " + validacionId));
        
        Usuario admin = (adminEmail != null) ? usuarioRepository.findByCorreo(adminEmail).orElse(null) : null;
        
        if (nuevoEstado == EstadoValidacion.VALIDADO) {
            validacion.aprobarVerificacion(admin);
        } else if (nuevoEstado == EstadoValidacion.RECHAZADO) {
            validacion.rechazarVerificacion(admin);
        } else {
            validacion.setEstadoValidacion(nuevoEstado);
        }
        validacionRepository.save(validacion);
    }

    @Transactional
    public void assignRoles(RoleAssignRequestDTO requestDTO) {
        Usuario usuario = usuarioRepository.findById(requestDTO.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Set<Rol> rolesAsignar = new HashSet<>();
        // Todo usuario registrado en el sistema cuenta con el rol base de Usuario Registrado (RF-03, HU6)
        rolRepository.findByNombre(RolNombre.ROLE_USUARIO.name()).ifPresent(rolesAsignar::add);

        for (String originalRolNombre : requestDTO.getRoles()) {
            RolNombre rolEnum = RolNombre.fromString(originalRolNombre);
            Rol rol = rolRepository.findByNombre(rolEnum.name())
                    .orElseThrow(() -> new ResourceNotFoundException("Rol institucional no encontrado: " + rolEnum.name()));
            rolesAsignar.add(rol);
        }

        // Criterio de Aceptación 3 de HU6: No permitir revocar el rol de Administrador general de la cuenta principal
        if ("admin@uagrm.edu.bo".equalsIgnoreCase(usuario.getCorreo()) &&
                rolesAsignar.stream().noneMatch(r -> r.getNombre().equals(RolNombre.ROLE_ADMIN.name()))) {
            throw new BadRequestException(
                    "No se permite revocar el rol de Administrador general de la cuenta principal del sistema.");
        }

        usuario.setRoles(rolesAsignar);
        usuarioRepository.save(usuario);
        log.info("Roles updated for user {}: {}", usuario.getId(), requestDTO.getRoles());
    }

    @Transactional
    public void aprobarValidacionPorUsuario(Long usuarioId, String adminEmail) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));
        
        ValidacionAcademica val = validacionRepository.findFirstByUsuarioOrderByIdDesc(usuario)
                .orElseGet(() -> {
                    ValidacionAcademica nueva = new ValidacionAcademica();
                    nueva.setUsuario(usuario);
                    nueva.setRegistroUagrm("REG-" + usuario.getId());
                    nueva.setIdentificadorInstitucional("REG-" + usuario.getId());
                    nueva.setTipo(TipoMiembro.ESTUDIANTE);
                    nueva.setTipoMiembro(TipoMiembro.ESTUDIANTE);
                    nueva.setTipoVerificacion(TipoVerificacion.DOCUMENTO_MANUAL);
                    return nueva;
                });
        
        Usuario admin = (adminEmail != null) ? usuarioRepository.findByCorreo(adminEmail).orElse(null) : null;
        val.aprobarVerificacion(admin);
        validacionRepository.save(val);
    }
}
