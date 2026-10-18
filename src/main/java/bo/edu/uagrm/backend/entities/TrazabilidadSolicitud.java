package bo.edu.uagrm.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trazabilidad_solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrazabilidadSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Publicacion solicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_alcanzado", nullable = false, length = 30)
    private EstadoPublicacion estadoAlcanzado;

    @Column(name = "descripcion_hito", nullable = false, length = 255)
    private String descripcionHito;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_responsable_id")
    private Usuario usuarioResponsable;

    @CreationTimestamp
    @Column(name = "fecha_hito", updatable = false)
    private LocalDateTime fechaHito;
}
