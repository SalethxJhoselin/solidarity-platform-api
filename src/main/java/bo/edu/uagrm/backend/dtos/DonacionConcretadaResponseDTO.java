package bo.edu.uagrm.backend.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonacionConcretadaResponseDTO {
    private Long id;
    private Long solicitudId;
    private String solicitudTitulo;
    private String solicitanteNombre;
    private String solicitanteCorreo;

    private Long donanteId;
    private String donanteNombre;
    private String donanteCorreo;

    private Integer cantidadEntregada;
    private String observaciones;
    private LocalDateTime fechaEntrega;
}
