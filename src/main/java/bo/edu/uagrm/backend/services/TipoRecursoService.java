package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.TipoRecursoDTO;
import bo.edu.uagrm.backend.entities.TipoRecurso;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.exceptions.ResourceNotFoundException;
import bo.edu.uagrm.backend.repositories.PublicacionRepository;
import bo.edu.uagrm.backend.repositories.TipoRecursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TipoRecursoService {

    private final TipoRecursoRepository tipoRecursoRepository;
    private final PublicacionRepository publicacionRepository;

    public TipoRecursoService(TipoRecursoRepository tipoRecursoRepository,
                              PublicacionRepository publicacionRepository) {
        this.tipoRecursoRepository = tipoRecursoRepository;
        this.publicacionRepository = publicacionRepository;
    }

    @Transactional(readOnly = true)
    public List<TipoRecursoDTO> obtenerTiposActivos() {
        return tipoRecursoRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TipoRecursoDTO> obtenerTodosTipos() {
        return tipoRecursoRepository.findAllByOrderByIdAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TipoRecursoDTO obtenerPorId(Long id) {
        TipoRecurso tr = tipoRecursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de recurso no encontrado con ID: " + id));
        return mapToDTO(tr);
    }

    @Transactional
    public TipoRecursoDTO crearTipoRecurso(TipoRecursoDTO dto) {
        if (tipoRecursoRepository.existsByNombreIgnoreCase(dto.getNombre().trim())) {
            throw new BadRequestException("Ya existe un tipo de recurso registrado con el nombre: " + dto.getNombre().trim());
        }

        TipoRecurso tipoRecurso = TipoRecurso.builder()
                .nombre(dto.getNombre().trim())
                .descripcion(dto.getDescripcion())
                .unidadMedida(dto.getUnidadMedida() != null ? dto.getUnidadMedida().trim() : "Unidades")
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        tipoRecurso = tipoRecursoRepository.save(tipoRecurso);
        return mapToDTO(tipoRecurso);
    }

    @Transactional
    public TipoRecursoDTO actualizarTipoRecurso(Long id, TipoRecursoDTO dto) {
        TipoRecurso tipoRecurso = tipoRecursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de recurso no encontrado con ID: " + id));

        if (tipoRecursoRepository.existsByNombreIgnoreCaseAndIdNot(dto.getNombre().trim(), id)) {
            throw new BadRequestException("Ya existe otro tipo de recurso con el nombre: " + dto.getNombre().trim());
        }

        tipoRecurso.setNombre(dto.getNombre().trim());
        tipoRecurso.setDescripcion(dto.getDescripcion());
        if (dto.getUnidadMedida() != null) {
            tipoRecurso.setUnidadMedida(dto.getUnidadMedida().trim());
        }
        if (dto.getActivo() != null) {
            tipoRecurso.setActivo(dto.getActivo());
        }

        tipoRecurso = tipoRecursoRepository.save(tipoRecurso);
        return mapToDTO(tipoRecurso);
    }

    @Transactional
    public String eliminarODesactivarTipoRecurso(Long id) {
        TipoRecurso tipoRecurso = tipoRecursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de recurso no encontrado con ID: " + id));

        boolean tienePublicaciones = publicacionRepository.existsByTipoRecursoId(id);

        if (tienePublicaciones) {
            // Cumplimiento Criterio de Aceptación 3 de HU8:
            // "los tipos de recurso solo pueden ser ocultados/inactivados si ya han sido utilizados en donaciones históricas"
            tipoRecurso.setActivo(false);
            tipoRecursoRepository.save(tipoRecurso);
            return "El tipo de recurso ya fue utilizado en donaciones históricas. Se ha marcado como Inactivo para preservar la integridad referencial.";
        } else {
            tipoRecursoRepository.delete(tipoRecurso);
            return "Tipo de recurso eliminado permanentemente con éxito.";
        }
    }

    @Transactional
    public TipoRecursoDTO cambiarEstado(Long id, boolean activo) {
        TipoRecurso tipoRecurso = tipoRecursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de recurso no encontrado con ID: " + id));
        tipoRecurso.setActivo(activo);
        tipoRecurso = tipoRecursoRepository.save(tipoRecurso);
        return mapToDTO(tipoRecurso);
    }

    public TipoRecursoDTO mapToDTO(TipoRecurso entity) {
        return TipoRecursoDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .unidadMedida(entity.getUnidadMedida())
                .activo(entity.getActivo())
                .fechaCreacion(entity.getFechaCreacion())
                .build();
    }
}
