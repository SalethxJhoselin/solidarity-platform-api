package bo.edu.uagrm.backend.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchivoAdjuntoDTO {
    private Long id;
    private String nombreOriginal;
    private String nombreAlmacenado;
    private String tipoMime;
    private Long tamanoBytes;
    private String urlDescarga;
    private boolean esImagen;
    private LocalDateTime fechaCreacion;
}
