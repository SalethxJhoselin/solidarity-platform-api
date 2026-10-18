package bo.edu.uagrm.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "donaciones_concretadas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonacionConcretada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Publicacion solicitud;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donante_id", nullable = false)
    private Usuario donante;

    @Column(name = "cantidad_entregada")
    private Integer cantidadEntregada;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_entrega", updatable = false)
    private LocalDateTime fechaEntrega;
}
