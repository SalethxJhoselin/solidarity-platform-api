package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.enums.*;

import bo.edu.uagrm.backend.dtos.*;
import bo.edu.uagrm.backend.entities.*;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.*;
import bo.edu.uagrm.backend.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final TokenRecuperacionContrasenaRepository tokenRepository;
    private final ValidacionAcademicaRepository validacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PerfilUsuarioRepository perfilUsuarioRepository,
            TokenRecuperacionContrasenaRepository tokenRepository,
            ValidacionAcademicaRepository validacionRepository,
            PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
            JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
        this.tokenRepository = tokenRepository;
        this.validacionRepository = validacionRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    public static boolean esDominioUagrm(String correo) {
        if (correo == null || !correo.contains("@"))
            return false;
        String dominio = correo.substring(correo.indexOf("@") + 1).toLowerCase().trim();
        return dominio.equals("uagrm.edu.bo") || dominio.endsWith(".uagrm.edu.bo");
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BadRequestException("El correo ya está en uso");
        }

        Usuario usuario = new Usuario();
        usuario.setCorreo(request.getCorreo().trim());
        usuario.setHashContrasena(passwordEncoder.encode(request.getPassword()));
        usuario.setEstado(EstadoUsuario.ACTIVO);

        // Asignar rol oficial de Usuario Registrado (RF-03, HU1)
        Rol rol = rolRepository.findByNombre(RolNombre.ROLE_USUARIO.name())
                .orElseThrow(() -> new ResourceNotFoundException("Rol por defecto ROLE_USUARIO no encontrado"));
        usuario.getRoles().add(rol);

        usuario = usuarioRepository.saveAndFlush(usuario);

        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setUsuario(usuario);
        perfil.setNombres(request.getNombres().trim());
        perfil.setApellidos(request.getApellidos().trim());
        entityManager.persist(perfil);

        // HU5: Validación automática para dominios @uagrm.edu.bo o *.uagrm.edu.bo
        boolean autoValidado = esDominioUagrm(usuario.getCorreo());
        EstadoValidacion estadoVal = autoValidado ? EstadoValidacion.VALIDADO : EstadoValidacion.PENDIENTE;

        ValidacionAcademica validacion = new ValidacionAcademica();
        validacion.setUsuario(usuario);
        validacion.setRegistroUagrm("REG-" + usuario.getId());
        validacion.setIdentificadorInstitucional("REG-" + usuario.getId());
        validacion.setTipo(TipoMiembro.ESTUDIANTE);
        validacion.setTipoMiembro(TipoMiembro.ESTUDIANTE);
        validacion.setEstadoValidacion(estadoVal);
        if (estadoVal == EstadoValidacion.VALIDADO) {
            validacion.setTipoVerificacion(TipoVerificacion.CORREO_INSTITUCIONAL);
            validacion.setFechaVerificacion(LocalDateTime.now());
        } else {
            validacion.setTipoVerificacion(TipoVerificacion.DOCUMENTO_MANUAL);
        }
        validacionRepository.save(validacion);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
        String token = jwtUtils.generateToken(userDetails);

        List<String> rolesStr = usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toList());

        AuthResponseDTO response = new AuthResponseDTO(token, usuario.getId(), usuario.getCorreo(), rolesStr);
        response.setEstado(usuario.getEstado());
        response.setEstadoValidacion(estadoVal);
        return response;
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword()));

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
        String token = jwtUtils.generateToken(userDetails);

        List<String> rolesStr = usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toList());

        EstadoValidacion estadoVal = EstadoValidacion.VALIDADO;
        if (!esDominioUagrm(usuario.getCorreo())) {
            estadoVal = validacionRepository.findFirstByUsuarioOrderByIdDesc(usuario)
                    .map(ValidacionAcademica::getEstadoValidacion)
                    .orElse(EstadoValidacion.PENDIENTE);
        }

        AuthResponseDTO response = new AuthResponseDTO(token, usuario.getId(), usuario.getCorreo(), rolesStr);
        response.setEstado(usuario.getEstado());
        response.setEstadoValidacion(estadoVal);
        return response;
    }

    @Transactional
    public String recoverPassword(RecoveryRequestDTO request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String token = UUID.randomUUID().toString();

        TokenRecuperacionContrasena recoveryToken = new TokenRecuperacionContrasena();
        recoveryToken.setUsuario(usuario);
        recoveryToken.setHashToken(token);
        recoveryToken.setFechaExpiracion(LocalDateTime.now().plusHours(1));
        tokenRepository.save(recoveryToken);

        return token;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        TokenRecuperacionContrasena recoveryToken = tokenRepository.findByHashToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Token inválido"));

        if (recoveryToken.getFechaUso() != null) {
            throw new BadRequestException("El token ya fue utilizado");
        }

        if (recoveryToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El token ha expirado");
        }

        Usuario usuario = recoveryToken.getUsuario();
        usuario.setHashContrasena(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);

        // HU3 Criterio 4: Invalidar inmediatamente todos los tokens anteriores del
        // usuario
        List<TokenRecuperacionContrasena> tokensUsuario = tokenRepository.findByUsuarioAndFechaUsoIsNull(usuario);
        LocalDateTime ahora = LocalDateTime.now();
        for (TokenRecuperacionContrasena t : tokensUsuario) {
            t.setFechaUso(ahora);
        }
        tokenRepository.saveAll(tokensUsuario);
    }
}
