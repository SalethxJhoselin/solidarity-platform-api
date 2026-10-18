package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.entities.TipoPublicacion;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiltroPublicacionDTO {
    private String query;
    private TipoPublicacion tipo;
    private Long categoriaId;
    private Long tipoRecursoId;
    private String orden; // recientes, antiguas
    
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 10;
}
