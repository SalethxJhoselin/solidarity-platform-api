package bo.edu.uagrm.backend.enums;

/**
 * Roles oficiales de seguridad del sistema (RBAC) según RF-03 y HU6 del documento oficial.
 */
public enum RolNombre {
    ROLE_ADMIN("ADMINISTRADOR"),
    ROLE_MODERADOR("MODERADOR"),
    ROLE_USUARIO("USUARIO");

    private final String cleanName;

    RolNombre(String cleanName) {
        this.cleanName = cleanName;
    }

    public String getCleanName() {
        return cleanName;
    }

    /**
     * Interpreta y normaliza nombres de rol provistos desde solicitudes externas.
     */
    public static RolNombre fromString(String text) {
        if (text == null || text.isBlank()) {
            return ROLE_USUARIO;
        }
        String clean = text.trim().toUpperCase();
        if (clean.equals("ADMINISTRADOR") || clean.equals("ADMIN") || clean.equals("ROLE_ADMIN")) {
            return ROLE_ADMIN;
        }
        if (clean.equals("MODERADOR") || clean.equals("ROLE_MODERADOR")) {
            return ROLE_MODERADOR;
        }
        return ROLE_USUARIO;
    }
}

