package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.ValidacionRequestDTO;
import bo.edu.uagrm.backend.dtos.ValidacionResponseDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.entities.ValidacionAcademica;
import bo.edu.uagrm.backend.enums.EstadoValidacion;
import bo.edu.uagrm.backend.enums.TipoMiembro;
import bo.edu.uagrm.backend.exceptions.BadRequestException;
import bo.edu.uagrm.backend.repositories.ValidacionAcademicaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ValidacionService {

    private final ValidacionAcademicaRepository validacionRepository;

    public ValidacionService(ValidacionAcademicaRepository validacionRepository) {
        this.validacionRepository = validacionRepository;
    }

    @Transactional
    public ValidacionResponseDTO solicitarValidacion(Usuario usuario, ValidacionRequestDTO requestDTO) {
        String nuevoRegistro = requestDTO.getRegistroUagrm().trim();
        TipoMiembro tipoMiembro = TipoMiembro.valueOf(requestDTO.getTipo().trim().toUpperCase());

        ValidacionAcademica existente = validacionRepository.findFirstByUsuarioOrderByIdDesc(usuario).orElse(null);

        if (existente == null) {
            if (validacionRepository.existsByRegistroUagrm(nuevoRegistro)) {
                throw new BadRequestException(
                        "El registro proporcionado ya se encuentra registrado o en proceso de validación");
            }
            ValidacionAcademica validacion = new ValidacionAcademica();
            validacion.setUsuario(usuario);
            validacion.setRegistroUagrm(nuevoRegistro);
            validacion.setIdentificadorInstitucional(nuevoRegistro);
            validacion.setTipo(tipoMiembro);
            validacion.setTipoMiembro(tipoMiembro);
            validacion.setEstadoValidacion(EstadoValidacion.PENDIENTE);
            validacionRepository.save(validacion);
        } else {
            if (!nuevoRegistro.equals(existente.getRegistroUagrm())
                    && validacionRepository.existsByRegistroUagrm(nuevoRegistro)) {
                throw new BadRequestException(
                        "El registro proporcionado ya se encuentra registrado o en proceso de validación");
            }
            existente.setRegistroUagrm(nuevoRegistro);
            existente.setIdentificadorInstitucional(nuevoRegistro);
            existente.setTipo(tipoMiembro);
            existente.setTipoMiembro(tipoMiembro);
            existente.setEstadoValidacion(EstadoValidacion.PENDIENTE);
            validacionRepository.save(existente);
        }

        return ValidacionResponseDTO.builder()
                .mensaje("Solicitud de validación enviada correctamente")
                .valido(true)
                .registroAcademico(nuevoRegistro)
                .registroUagrm(nuevoRegistro)
                .estado(EstadoValidacion.PENDIENTE)
                .build();
    }

    @Transactional(readOnly = true)
    public ValidacionResponseDTO obtenerEstadoValidacion(Usuario usuario) {
        ValidacionAcademica val = validacionRepository.findFirstByUsuarioOrderByIdDesc(usuario).orElse(null);
        if (val == null) {
            return ValidacionResponseDTO.builder()
                    .mensaje("Sin validación académica registrada")
                    .valido(false)
                    .estado(EstadoValidacion.PENDIENTE)
                    .build();
        }
        return ValidacionResponseDTO.builder()
                .mensaje("Estado de validación obtenido")
                .valido(val.getEstadoValidacion() == EstadoValidacion.VALIDADO)
                .registroAcademico(val.getRegistroUagrm())
                .registroUagrm(val.getRegistroUagrm())
                .estado(val.getEstadoValidacion())
                .build();
    }
}
