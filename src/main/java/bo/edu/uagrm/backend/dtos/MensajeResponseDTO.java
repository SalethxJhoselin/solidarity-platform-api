package bo.edu.uagrm.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponseDTO {
    private String mensaje;
    private Long id;
    private Object data;

    public static MensajeResponseDTO of(String mensaje) {
        return MensajeResponseDTO.builder().mensaje(mensaje).build();
    }

    public static MensajeResponseDTO of(String mensaje, Long id) {
        return MensajeResponseDTO.builder().mensaje(mensaje).id(id).build();
    }

    public static MensajeResponseDTO of(String mensaje, Long id, Object data) {
        return MensajeResponseDTO.builder().mensaje(mensaje).id(id).data(data).build();
    }
}

