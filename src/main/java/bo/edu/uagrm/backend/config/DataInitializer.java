package bo.edu.uagrm.backend.config;

import bo.edu.uagrm.backend.entities.PerfilUsuario;
import bo.edu.uagrm.backend.entities.Rol;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import bo.edu.uagrm.backend.repositories.RolRepository;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PerfilUsuarioRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           PerfilUsuarioRepository perfilRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Asegurar que existan todos los roles institucionales
        ensureRol("ROLE_ADMIN", "Administrador del sistema");
        ensureRol("ROLE_SOLICITANTE", "Usuario solicitante de ayuda");
        ensureRol("ROLE_VOLUNTARIO", "Usuario voluntario");
        ensureRol("ROLE_DONANTE", "Usuario donante");
        ensureRol("ROLE_ESTUDIANTE", "Estudiante de la UAGRM");

        // 1. Cuenta Administrador Institucional
        initUser("admin@uagrm.edu.bo", "Admin123!", "Administrador", "UAGRM",
                "Rectorado y Bienestar Estudiantil", "Administración Institucional", "71000001",
                Set.of("ROLE_ADMIN", "ROLE_SOLICITANTE"));

        // 2. Cuenta Voluntario Saleth
        initUser("saleth@ficct.uagrm.edu.bo", "Saleth123!", "Saleth", "Jhoselin",
                "Facultad de Ingeniería en Ciencias de la Computación y Telecomunicaciones", "Ingeniería Informática", "78945612",
                Set.of("ROLE_VOLUNTARIO", "ROLE_ESTUDIANTE"));

        // 3. Cuenta Demo (Andrea Flores)
        initUser("demo@uagrm.edu.bo", "Demo123!", "Andrea", "Flores",
                "Facultad de Ciencias Económicas y Financieras", "Ingeniería Comercial", "71234567",
                Set.of("ROLE_SOLICITANTE", "ROLE_VOLUNTARIO"));

        // 4. Carlos Suárez (Voluntario)
        initUser("carlos.suarez@demo.uagrm.bo", "Demo123!", "Carlos Andrés", "Suárez",
                "Facultad de Humanidades", "Psicología", "72345678",
                Set.of("ROLE_VOLUNTARIO", "ROLE_ESTUDIANTE"));

        // 5. Daniela Vargas (Donante)
        initUser("daniela.vargas@demo.uagrm.bo", "Demo123!", "Daniela", "Vargas",
                "Facultad de Ciencias de la Salud", "Medicina", "73456789",
                Set.of("ROLE_DONANTE", "ROLE_ESTUDIANTE"));

        // 6. María Fernanda Rojas (Solicitante)
        initUser("maria.rojas@demo.uagrm.bo", "Demo123!", "María Fernanda", "Rojas",
                "Facultad Politécnica", "Ofimática", "74567890",
                Set.of("ROLE_SOLICITANTE", "ROLE_ESTUDIANTE"));
    }

    private void ensureRol(String nombre, String descripcion) {
        if (!rolRepository.findByNombre(nombre).isPresent()) {
            Rol r = new Rol();
            r.setNombre(nombre);
            r.setDescripcion(descripcion);
            rolRepository.save(r);
        }
    }

    private void initUser(String correo, String plainPassword, String nombres, String apellidos,
                          String facultad, String carrera, String telefono, Set<String> roleNames) {
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setCorreo(correo);
            usuario.setRoles(new HashSet<>());
        }

        usuario.setHashContrasena(passwordEncoder.encode(plainPassword));
        usuario.setEstado("ACTIVO");

        for (String roleName : roleNames) {
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
        perfil.setDescripcion("Perfil oficial verificado de la comunidad solidaria UAGRM");
        perfilRepository.save(perfil);
    }
}
