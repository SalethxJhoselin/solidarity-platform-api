package bo.edu.uagrm.backend.entities;

import bo.edu.uagrm.backend.enums.*;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(name = "hash_contrasena", nullable = false)
    private String hashContrasena;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuarios_roles", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();

    public boolean tieneRol(RolNombre rolNombre) {
        if (roles == null)
            return false;
        return roles.stream().anyMatch(r -> r.getNombre().equalsIgnoreCase(rolNombre.name()));
    }

    public boolean esAdmin() {
        return tieneRol(RolNombre.ROLE_ADMIN);
    }

    public boolean esModerador() {
        return tieneRol(RolNombre.ROLE_MODERADOR);
    }
}
