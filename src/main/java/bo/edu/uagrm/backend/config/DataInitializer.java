package bo.edu.uagrm.backend.config;

import bo.edu.uagrm.backend.enums.*;

import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import bo.edu.uagrm.backend.repositories.RolRepository;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import bo.edu.uagrm.backend.repositories.ValidacionAcademicaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Inicializador de datos institucionales del sistema.
 * 
 * Según la especificación oficial del proyecto (RF-03, HU6 y Párrafo 456):
 * - Los perfiles de acceso son: Usuario Visitante (no autenticado), Usuario
 * Registrado,
 * Moderador y Administrador general.
 * - Solicitante, Voluntario y Donante NO son roles del sistema; son tipos de
 * publicación
 * que cualquier Usuario Registrado realiza en la plataforma.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PerfilUsuarioRepository perfilRepository;
    private final ValidacionAcademicaRepository validacionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PerfilUsuarioRepository perfilRepository,
            ValidacionAcademicaRepository validacionRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.perfilRepository = perfilRepository;
        this.validacionRepository = validacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Roles oficiales del sistema (RF-03, HU6):
        // Administrador general, Moderador, Usuario Registrado
        ensureRol(RolNombre.ROLE_ADMIN.name(), "Administrador general del sistema");
        ensureRol(RolNombre.ROLE_MODERADOR.name(), "Moderador de contenido de la comunidad");
        ensureRol(RolNombre.ROLE_USUARIO.name(), "Usuario Registrado de la comunidad universitaria");

        // Cuenta del Administrador General del sistema
        initAdminUser("admin@uagrm.edu.bo", "Admin123!", "Administrador", "UAGRM",
                "Rectorado y Bienestar Estudiantil", "Administración Institucional", "71000001");

        // Cuenta de la administradora Saleth (Product Owner del proyecto)
        initAdminUser("saleth@ficct.uagrm.edu.bo", "Admin123$", "Saleth", "Jhoselin",
                "Facultad de Ingeniería en Ciencias de la Computación y Telecomunicaciones",
                "Ingeniería Informática", "78945612");
    }

    private void ensureRol(String nombre, String descripcion) {
        if (!rolRepository.findByNombre(nombre).isPresent()) {
            Rol r = new Rol();
            r.setNombre(nombre);
            r.setDescripcion(descripcion);
            rolRepository.save(r);
        }
    }

    private void initAdminUser(String correo, String plainPassword, String nombres, String apellidos,
            String facultad, String carrera, String telefono) {
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setCorreo(correo);
            usuario.setRoles(new HashSet<>());
        }

        usuario.setHashContrasena(passwordEncoder.encode(plainPassword));
        usuario.setEstado(EstadoUsuario.ACTIVO);

        // El Administrador general cuenta con permisos de administración y acceso como
        // usuario
        for (String roleName : Set.of(RolNombre.ROLE_ADMIN.name(), RolNombre.ROLE_USUARIO.name())) {
            Rol r = rolRepository.findByNombre(roleName).orElse(null);
            if (r != null) {
                usuario.getRoles().add(r);
            }
        }

        usuario = usuarioRepository.saveAndFlush(usuario);

        PerfilUsuario perfil = perfilRepository.findById(usuario.getId()).orElse(null);
        if (perfil == null) {
            perfil = new PerfilUsuario();
            perfil.setUsuario(usuario);
        }
        perfil.setNombres(nombres);
        perfil.setApellidos(apellidos);
        perfil.setFacultad(facultad);
        perfil.setCarrera(carrera);
        perfil.setTelefono(telefono);
        perfil.setDescripcion("Administrador general de la plataforma solidaria UAGRM");
        perfilRepository.save(perfil);

        // Auto-validación institucional para correos @uagrm.edu.bo (RF-05)
        ValidacionAcademica validacion = validacionRepository.findFirstByUsuarioOrderByIdDesc(usuario).orElse(null);
        if (validacion == null) {
            validacion = new ValidacionAcademica();
            validacion.setUsuario(usuario);
            validacion.setRegistroUagrm("REG-ADMIN-" + usuario.getId());
            validacion.setIdentificadorInstitucional("REG-ADMIN-" + usuario.getId());
            validacion.setTipo(TipoMiembro.DOCENTE);
            validacion.setTipoMiembro(TipoMiembro.DOCENTE);
            validacion.setTipoVerificacion(TipoVerificacion.CORREO_INSTITUCIONAL);
            validacion.setFechaVerificacion(LocalDateTime.now());
        }
        validacion.setEstadoValidacion(EstadoValidacion.VALIDADO);
        validacionRepository.save(validacion);
    }
}
