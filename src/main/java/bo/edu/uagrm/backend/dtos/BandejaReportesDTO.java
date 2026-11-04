package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.enums.MotivoReporte;
import bo.edu.uagrm.backend.enums.TipoPublicacion;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BandejaReportesDTO {
    private Long publicacionId;
    private String publicacionTitulo;
    private String autorNombre;
    private String autorCorreo;
    private TipoPublicacion tipoPublicacion;
    private int totalReportes;
    private List<MotivoReporte> motivos;
    private LocalDateTime fechaUltimoReporte;
    private List<ReporteResponseDTO> reportesDetalle;
}
