package bo.edu.uagrm.backend.security;

import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.enums.RolNombre;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Thread-safe implementation of {@link AuthenticationFacade} backed by Spring Security's SecurityContextHolder.
 */
@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    private final UsuarioRepository usuarioRepository;

    public AuthenticationFacadeImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public Optional<String> getCurrentUserEmail() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.ofNullable(auth.getName());
    }

    @Override
    public Usuario getAuthenticatedUser() {
        String email = getCurrentUserEmail()
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated security context not found"));
        return usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found with email: " + email));
    }

    @Override
    public boolean isAdmin() {
        Authentication auth = getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RolNombre.ROLE_ADMIN.name()));
    }

    @Override
    public boolean isModerator() {
        Authentication auth = getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RolNombre.ROLE_MODERADOR.name())
                            || a.getAuthority().equals(RolNombre.ROLE_ADMIN.name()));
    }
}

