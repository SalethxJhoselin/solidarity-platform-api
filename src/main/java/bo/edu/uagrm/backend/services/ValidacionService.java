package bo.edu.uagrm.backend.services;

import bo.edu.uagrm.backend.dtos.ValidacionRequestDTO;
import bo.edu.uagrm.backend.entities.Usuario;
import bo.edu.uagrm.backend.entities.ValidacionAcademica;
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
    public void solicitarValidacion(Usuario usuario, ValidacionRequestDTO requestDTO) {
        if (validacionRepository.existsByRegistroUagrm(requestDTO.getRegistroUagrm())) {
            throw new BadRequestException("El registro proporcionado ya se encuentra registrado o en proceso de validación");
        }

        ValidacionAcademica validacion = new ValidacionAcademica();
        validacion.setUsuario(usuario);
        validacion.setRegistroUagrm(requestDTO.getRegistroUagrm());
        validacion.setTipo(requestDTO.getTipo().toUpperCase());
        validacion.setEstadoValidacion("PENDIENTE");

        validacionRepository.save(validacion);
    }
}
