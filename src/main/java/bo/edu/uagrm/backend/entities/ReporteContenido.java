package bo.edu.uagrm.backend.entities;

import bo.edu.uagrm.backend.enums.*;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_contenido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteContenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "denunciante_id", nullable = false)
    private Usuario denunciante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "publicacion_id")
    private Publicacion publicacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comentario_id")
    private Comentario comentario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MotivoReporte motivo;

    @Column(columnDefinition = "TEXT")
    private String detalles;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_reporte", nullable = false, length = 30)
    @Builder.Default
    private EstadoReporte estadoReporte = EstadoReporte.PENDIENTE;

    @Column(name = "resolucion_admin", columnDefinition = "TEXT")
    private String resolucionAdmin;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
}
