package bo.edu.uagrm.backend.mappers;

import bo.edu.uagrm.backend.dtos.ArchivoAdjuntoDTO;
import bo.edu.uagrm.backend.dtos.CategoriaDTO;
import bo.edu.uagrm.backend.dtos.DisponibilidadDTO;
import bo.edu.uagrm.backend.dtos.PublicacionResponseDTO;
import bo.edu.uagrm.backend.entities.PerfilUsuario;
import bo.edu.uagrm.backend.entities.Publicacion;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.repositories.PerfilUsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enterprise mapper responsible for transforming {@link Publicacion} domain entities
 * into {@link PublicacionResponseDTO} client contract representations.
 */
@Component
public class PublicacionMapper {

    private final PerfilUsuarioRepository perfilUsuarioRepository;

    public PublicacionMapper(PerfilUsuarioRepository perfilUsuarioRepository) {
        this.perfilUsuarioRepository = perfilUsuarioRepository;
    }

    public PublicacionResponseDTO toDTO(Publicacion pub) {
        if (pub == null) {
            return null;
        }

        Usuario usuario = pub.getUsuario();
        PerfilUsuario perfil = (usuario != null)
                ? perfilUsuarioRepository.findById(usuario.getId()).orElse(null)
                : null;

        String autorNombre = (perfil != null)
                ? perfil.getNombres() + " " + perfil.getApellidos()
                : (usuario != null ? usuario.getCorreo() : "Usuario UAGRM");
        String autorCarrera = (perfil != null) ? perfil.getCarrera() : null;
        String autorFacultad = (perfil != null) ? perfil.getFacultad() : null;
        String autorTelefono = (perfil != null) ? perfil.getTelefono() : null;
        String autorFoto = (perfil != null) ? perfil.getUrlFotoPerfil() : null;

        List<CategoriaDTO> secundariasDTO = (pub.getCategoriasSecundarias() != null)
                ? pub.getCategoriasSecundarias().stream()
                        .map(c -> CategoriaDTO.builder()
                                .id(c.getId())
                                .nombre(c.getNombre())
                                .descripcion(c.getDescripcion())
                                .icono(c.getIcono())
                                .activo(c.getActivo())
                                .build())
                        .collect(Collectors.toList())
                : Collections.emptyList();

        List<DisponibilidadDTO> dispsDTO = (pub.getDisponibilidades() != null)
                ? pub.getDisponibilidades().stream()
                        .map(d -> DisponibilidadDTO.builder()
                                .diaSemana(d.getDiaSemana())
                                .turno(d.getTurno())
                                .build())
                        .collect(Collectors.toList())
                : Collections.emptyList();

        List<ArchivoAdjuntoDTO> archivosDTO = (pub.getArchivos() != null)
                ? pub.getArchivos().stream()
                        .map(a -> ArchivoAdjuntoDTO.builder()
                                .id(a.getId())
                                .nombreOriginal(a.getNombreOriginal())
                                .nombreAlmacenado(a.getNombreAlmacenado())
                                .tipoMime(a.getTipoMime())
                                .tamanoBytes(a.getTamanoBytes())
                                .urlDescarga(a.getUrlDescarga())
                                .esImagen(a.getTipoMime() != null && a.getTipoMime().startsWith("image/"))
                                .fechaCreacion(a.getFechaCreacion())
                                .build())
                        .collect(Collectors.toList())
                : Collections.emptyList();

        int cantidadComentarios = (pub.getComentarios() != null)
                ? (int) pub.getComentarios().stream().filter(c -> Boolean.TRUE.equals(c.getActivo())).count()
                : 0;

        return PublicacionResponseDTO.builder()
                .id(pub.getId())
                .usuarioId(usuario != null ? usuario.getId() : null)
                .autorNombre(autorNombre)
                .autorCorreo(usuario != null ? usuario.getCorreo() : null)
                .autorCarrera(autorCarrera)
                .autorFacultad(autorFacultad)
                .autorTelefono(autorTelefono)
                .autorFotoPerfil(autorFoto)
                .tipo(pub.getTipo())
                .titulo(pub.getTitulo())
                .descripcion(pub.getDescripcion())
                .categoriaId(pub.getCategoria() != null ? pub.getCategoria().getId() : null)
                .categoriaNombre(pub.getCategoria() != null ? pub.getCategoria().getNombre() : null)
                .categoriaIcono(pub.getCategoria() != null ? pub.getCategoria().getIcono() : null)
                .categoriasSecundarias(secundariasDTO)
                .estado(pub.getEstado())
                .activo(pub.getActivo())
                .tipoRecursoId(pub.getTipoRecurso() != null ? pub.getTipoRecurso().getId() : null)
                .tipoRecursoNombre(pub.getTipoRecurso() != null ? pub.getTipoRecurso().getNombre() : null)
                .tipoRecursoUnidad(pub.getTipoRecurso() != null ? pub.getTipoRecurso().getUnidadMedida() : null)
                .cantidadTotal(pub.getCantidadTotal())
                .cantidadDisponible(pub.getCantidadDisponible())
                .fechaInicio(pub.getFechaInicio())
                .fechaFin(pub.getFechaFin())
                .lugar(pub.getLugar())
                .disponibilidadHoraria(pub.getDisponibilidadHoraria())
                .disponibilidades(dispsDTO)
                .archivos(archivosDTO)
                .cantidadComentarios(cantidadComentarios)
                .fechaCreacion(pub.getFechaCreacion())
                .fechaActualizacion(pub.getFechaActualizacion())
                .build();
    }
}

