package bo.edu.uagrm.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "validaciones_academicas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ValidacionAcademica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "registro_uagrm", nullable = false, unique = true, length = 50)
    private String registroUagrm;

    @Column(nullable = false, length = 30)
    private String tipo; // ESTUDIANTE, DOCENTE

    @Column(name = "estado_validacion", nullable = false, length = 30)
    private String estadoValidacion = "PENDIENTE"; // PENDIENTE, VALIDADO, RECHAZADO

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}
