package bo.edu.uagrm.backend.entities;

import bo.edu.uagrm.backend.enums.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "disponibilidades_voluntario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadVoluntario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publicacion_id", nullable = false)
    @JsonIgnore
    private Publicacion publicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 30)
    private DiaSemana diaSemana;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Turno turno;
}
