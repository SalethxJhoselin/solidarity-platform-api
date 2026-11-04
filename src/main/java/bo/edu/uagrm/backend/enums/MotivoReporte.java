package bo.edu.uagrm.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MotivoReporte {
    SPAM_O_PUBLICIDAD,
    CONTENIDO_OFENSIVO,
    INFORMACION_FALSA_O_FRAUDE,
    ACOSO_O_DISCRIMINACION,
    OTRO;

    @JsonCreator
    public static MotivoReporte fromString(String value) {
        if (value == null) return OTRO;
        String normalized = value.trim().toUpperCase();
        if (normalized.equals("SPAM")) return SPAM_O_PUBLICIDAD;
        if (normalized.equals("OFENSIVO") || normalized.equals("INAPPROPRIATE_CONTENT")) return CONTENIDO_OFENSIVO;
        if (normalized.equals("FRAUDE") || normalized.equals("FALSE_INFORMATION")) return INFORMACION_FALSA_O_FRAUDE;
        if (normalized.equals("ACOSO") || normalized.equals("HARASSMENT")) return ACOSO_O_DISCRIMINACION;
        for (MotivoReporte m : values()) {
            if (m.name().equalsIgnoreCase(normalized)) return m;
        }
        return OTRO;
    }
}
