package bo.edu.uagrm.backend.security;

import bo.edu.uagrm.backend.entities.Usuario;
import org.springframework.security.core.Authentication;

import java.util.Optional;

/**
 * Enterprise contract to access the current security and user context in a decoupled, testable manner.
 */
public interface AuthenticationFacade {

    /**
     * Retrieves the current Spring Security Authentication object.
     *
     * @return active Authentication, or null if unauthenticated.
     */
    Authentication getAuthentication();

    /**
     * Returns the username / email of the authenticated principal if present.
     *
     * @return Optional containing the email, or empty if anonymous.
     */
    Optional<String> getCurrentUserEmail();

    /**
     * Resolves and returns the fully populated domain Usuario entity from the database.
     *
     * @return active Usuario
     * @throws bo.edu.uagrm.backend.exceptions.ResourceNotFoundException if user or session is not found
     */
    Usuario getAuthenticatedUser();

    /**
     * Checks if the currently authenticated user possesses Administrator privileges (ROLE_ADMIN).
     *
     * @return true if administrator, false otherwise
     */
    boolean isAdmin();

    /**
     * Checks if the currently authenticated user possesses Moderator or Administrator privileges.
     *
     * @return true if moderator or admin, false otherwise
     */
    boolean isModerator();
}

