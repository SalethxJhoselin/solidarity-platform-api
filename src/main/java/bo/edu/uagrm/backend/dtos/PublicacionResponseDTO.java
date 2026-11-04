package bo.edu.uagrm.backend.dtos;

import bo.edu.uagrm.backend.enums.EstadoPublicacion;
import bo.edu.uagrm.backend.enums.TipoPublicacion;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicacionResponseDTO {

    private Long id;
    private Long usuarioId;
    private String autorNombre;
    private String autorCorreo;
    private String autorCarrera;
    private String autorFacultad;
    private String autorTelefono;
    private String autorFotoPerfil;

    private TipoPublicacion tipo;
    private String titulo;
    private String descripcion;

    private Long categoriaId;
    private String categoriaNombre;
    private String categoriaIcono;

    private List<CategoriaDTO> categoriasSecundarias;

    private EstadoPublicacion estado;
    private Boolean activo;

    // Donación
    private Long tipoRecursoId;
    private String tipoRecursoNombre;
    private String tipoRecursoUnidad;
    private Integer cantidadTotal;
    private Integer cantidadDisponible;

    // Actividad Colectiva
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String lugar;

    // Voluntariado
    private String disponibilidadHoraria;
    private List<DisponibilidadDTO> disponibilidades;

    // Evidencias y archivos adjuntos (HU14)
    private List<ArchivoAdjuntoDTO> archivos;
    private Integer cantidadComentarios;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
