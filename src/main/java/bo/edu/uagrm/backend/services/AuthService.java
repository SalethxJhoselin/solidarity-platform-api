package bo.edu.uagrm.backend.services;

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
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public AuthService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                       PerfilUsuarioRepository perfilUsuarioRepository,
                       TokenRecuperacionContrasenaRepository tokenRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.perfilUsuarioRepository = perfilUsuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BadRequestException("El correo ya está en uso");
        }

        Usuario usuario = new Usuario();
        usuario.setCorreo(request.getCorreo());
        usuario.setHashContrasena(passwordEncoder.encode(request.getPassword()));
        
        // Asignar rol por defecto a un visitante que se registra (ej. ROLE_SOLICITANTE o se le puede dar ROLE_USER)
        Rol rol = rolRepository.findByNombre("ROLE_SOLICITANTE")
                .orElseThrow(() -> new ResourceNotFoundException("Rol por defecto no encontrado"));
        usuario.getRoles().add(rol);
        
        usuario = usuarioRepository.saveAndFlush(usuario);

        PerfilUsuario perfil = new PerfilUsuario();
        perfil.setUsuario(usuario);
        perfil.setNombres(request.getNombres());
        perfil.setApellidos(request.getApellidos());
        entityManager.persist(perfil);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
        String token = jwtUtils.generateToken(userDetails);

        List<String> rolesStr = usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toList());

        return new AuthResponseDTO(token, usuario.getId(), usuario.getCorreo(), rolesStr);
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
        String token = jwtUtils.generateToken(userDetails);

        List<String> rolesStr = usuario.getRoles().stream().map(Rol::getNombre).collect(Collectors.toList());

        return new AuthResponseDTO(token, usuario.getId(), usuario.getCorreo(), rolesStr);
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

        // Simulate sending email
        return token; // En un entorno real, no se devuelve el token al cliente, se envía por correo.
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

        recoveryToken.setFechaUso(LocalDateTime.now());
        tokenRepository.save(recoveryToken);
    }
}
