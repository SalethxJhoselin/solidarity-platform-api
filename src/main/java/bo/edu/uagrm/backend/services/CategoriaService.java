package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.CategoriaDTO;
import bo.edu.uagrm.backend.entities.CategoriaPublicacion;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.CategoriaPublicacionRepository;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaPublicacionRepository categoriaRepository;
    private final PublicacionRepository publicacionRepository;

    public CategoriaService(CategoriaPublicacionRepository categoriaRepository,
                            PublicacionRepository publicacionRepository) {
        this.categoriaRepository = categoriaRepository;
        this.publicacionRepository = publicacionRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> obtenerCategoriasActivas() {
        return categoriaRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> obtenerTodasCategorias() {
        return categoriaRepository.findAllByOrderByIdAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaDTO obtenerPorId(Long id) {
        CategoriaPublicacion cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        return mapToDTO(cat);
    }

    @Transactional
    public CategoriaDTO crearCategoria(CategoriaDTO dto) {
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre().trim())) {
            throw new BadRequestException("Ya existe una categoría registrada con el nombre: " + dto.getNombre().trim());
        }

        CategoriaPublicacion categoria = CategoriaPublicacion.builder()
                .nombre(dto.getNombre().trim())
                .descripcion(dto.getDescripcion())
                .icono(dto.getIcono() != null ? dto.getIcono() : "FaFolder")
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        categoria = categoriaRepository.save(categoria);
        return mapToDTO(categoria);
    }

    @Transactional
    public CategoriaDTO actualizarCategoria(Long id, CategoriaDTO dto) {
        CategoriaPublicacion categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(dto.getNombre().trim(), id)) {
            throw new BadRequestException("Ya existe otra categoría registrada con el nombre: " + dto.getNombre().trim());
        }

        categoria.setNombre(dto.getNombre().trim());
        categoria.setDescripcion(dto.getDescripcion());
        if (dto.getIcono() != null) {
            categoria.setIcono(dto.getIcono());
        }
        if (dto.getActivo() != null) {
            categoria.setActivo(dto.getActivo());
        }

        categoria = categoriaRepository.save(categoria);
        return mapToDTO(categoria);
    }

    @Transactional
    public String eliminarODesactivarCategoria(Long id) {
        CategoriaPublicacion categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));

        boolean tienePublicaciones = publicacionRepository.existsByCategoriaId(id);

        if (tienePublicaciones) {
            // Cumplimiento Criterio de Aceptación 2 de HU7:
            // "impedir la eliminación física de una categoría si ya existen publicaciones asociadas; en su lugar, cambiar su estado a Inactiva"
            categoria.setActivo(false);
            categoriaRepository.save(categoria);
            return "La categoría tiene publicaciones vinculadas históricamente. Para preservar la integridad referencial, su estado ha sido cambiado a Inactiva.";
        } else {
            categoriaRepository.delete(categoria);
            return "Categoría eliminada permanentemente con éxito.";
        }
    }

    @Transactional
    public CategoriaDTO cambiarEstado(Long id, boolean activo) {
        CategoriaPublicacion categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
        categoria.setActivo(activo);
        categoria = categoriaRepository.save(categoria);
        return mapToDTO(categoria);
    }

    public CategoriaDTO mapToDTO(CategoriaPublicacion entity) {
        return CategoriaDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .icono(entity.getIcono())
                .activo(entity.getActivo())
                .fechaCreacion(entity.getFechaCreacion())
                .build();
    }
}
