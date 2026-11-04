package bo.edu.uagrm.backend.entities;

import bo.edu.uagrm.backend.enums.*;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMiembro tipo = TipoMiembro.ESTUDIANTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false, length = 30)
    private EstadoValidacion estadoValidacion = EstadoValidacion.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_miembro", length = 30)
    private TipoMiembro tipoMiembro;

    @Column(name = "identificador_institucional", length = 100)
    private String identificadorInstitucional;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_verificacion", length = 40)
    private TipoVerificacion tipoVerificacion = TipoVerificacion.DOCUMENTO_MANUAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verificado_por_usuario_id")
    private Usuario verificadoPor;

    @Column(name = "fecha_verificacion")
    private LocalDateTime fechaVerificacion;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    @PreUpdate
    public void syncDomainFields() {
        if (this.tipoMiembro == null && this.tipo != null) {
            this.tipoMiembro = this.tipo;
        }
        if (this.tipo == null && this.tipoMiembro != null) {
            this.tipo = this.tipoMiembro;
        }
        if (this.identificadorInstitucional == null && this.registroUagrm != null) {
            this.identificadorInstitucional = this.registroUagrm;
        }
        if (this.registroUagrm == null && this.identificadorInstitucional != null) {
            this.registroUagrm = this.identificadorInstitucional;
        }
    }

    public void aprobarVerificacion(Usuario adminVerificador) {
        this.estadoValidacion = EstadoValidacion.VALIDADO;
        this.verificadoPor = adminVerificador;
        this.fechaVerificacion = LocalDateTime.now();
    }

    public void rechazarVerificacion(Usuario adminVerificador) {
        this.estadoValidacion = EstadoValidacion.RECHAZADO;
        this.verificadoPor = adminVerificador;
        this.fechaVerificacion = LocalDateTime.now();
    }
}
