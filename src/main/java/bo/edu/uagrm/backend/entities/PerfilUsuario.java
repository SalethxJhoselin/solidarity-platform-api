package bo.edu.uagrm.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "perfiles_usuario")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PerfilUsuario {
    @Id
    @Column(name = "usuario_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 150)
    private String facultad;

    @Column(length = 150)
    private String carrera;

    @Column(name = "url_foto_perfil", length = 500)
    private String urlFotoPerfil;

    @Column(length = 30)
    private String telefono;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
