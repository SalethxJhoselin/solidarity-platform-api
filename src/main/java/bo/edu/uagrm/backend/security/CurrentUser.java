package bo.edu.uagrm.backend.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom controller parameter annotation that resolves and binds the currently authenticated {@link bo.edu.uagrm.backend.entities.Usuario}.
 *
 * Eliminates boilerplate security lookup code across REST controllers, enforcing DRY and clean architecture.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {

    /**
     * Whether an authenticated user is strictly required. Defaults to true.
     * When set to false, resolves to null if the user is unauthenticated.
     */
    boolean required() default true;
}

