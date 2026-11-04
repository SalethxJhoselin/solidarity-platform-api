package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.*;
import bo.edu.uagrm.backend.enums.EstadoUsuario;
import bo.edu.uagrm.backend.enums.EstadoValidacion;
import bo.edu.uagrm.backend.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "Administración General", description = "Endpoints de administración de usuarios, asignación de roles y aprobación de validaciones académicas (HU4, HU5, HU6)")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping({"/users", "/usuarios"})
    @Operation(summary = "Listar usuarios registrados (HU4, HU6)", description = "Retorna el listado completo de usuarios con roles y estado de validación")
    public ResponseEntity<List<UsuarioAdminDTO>> listarUsuarios() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PutMapping({"/users/{id}/status", "/usuarios/{id}/estado"})
    @Operation(summary = "Cambiar estado de cuenta de usuario (HU4, HU6)", description = "Permite activar, desactivar o suspender una cuenta de usuario")
    public ResponseEntity<MensajeResponseDTO> cambiarEstadoUsuario(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoUsuarioRequestDTO request) {
        adminService.cambiarEstadoUsuario(id, request.getEstado());
        return ResponseEntity.ok(MensajeResponseDTO.of("Estado de usuario actualizado correctamente a " + request.getEstado(), id, Map.of("estado", request.getEstado())));
    }

    @PostMapping({"/assign-roles", "/asignar-roles"})
    @Operation(summary = "Asignar roles institucionales (HU6)", description = "Permite asignar o revocar roles de Moderador y Administrador general")
    public ResponseEntity<MensajeResponseDTO> asignarRoles(@Valid @RequestBody RoleAssignRequestDTO requestDTO) {
        adminService.assignRoles(requestDTO);
        return ResponseEntity.ok(MensajeResponseDTO.of(
            "Roles asignados correctamente al usuario ID: " + requestDTO.getUsuarioId(),
            requestDTO.getUsuarioId(),
            Map.of("roles", requestDTO.getRoles())
        ));
    }

    @GetMapping({"/validations", "/validaciones"})
    @Operation(summary = "Listar solicitudes de validación académica (HU5)", description = "Retorna las solicitudes pendientes y resueltas de validación de pertenencia")
    public ResponseEntity<List<ValidacionAdminDTO>> listarValidaciones() {
        return ResponseEntity.ok(adminService.listValidaciones());
    }

    @PutMapping({"/validations/{id}/approve", "/validaciones/{id}/aprobar"})
    @Operation(summary = "Aprobar validación académica (HU5)")
    public ResponseEntity<MensajeResponseDTO> aprobarValidacion(
            @PathVariable Long id,
            Authentication authentication) {
        String adminEmail = (authentication != null) ? authentication.getName() : null;
        adminService.cambiarEstadoValidacion(id, EstadoValidacion.VALIDADO, adminEmail);
        return ResponseEntity.ok(MensajeResponseDTO.of("Validación académica aprobada", id, Map.of("estado", EstadoValidacion.VALIDADO)));
    }

    @PutMapping({"/validations/{id}/reject", "/validaciones/{id}/rechazar"})
    @Operation(summary = "Rechazar validación académica (HU5)")
    public ResponseEntity<MensajeResponseDTO> rechazarValidacion(
            @PathVariable Long id,
            Authentication authentication) {
        String adminEmail = (authentication != null) ? authentication.getName() : null;
        adminService.cambiarEstadoValidacion(id, EstadoValidacion.RECHAZADO, adminEmail);
        return ResponseEntity.ok(MensajeResponseDTO.of("Validación académica rechazada", id, Map.of("estado", EstadoValidacion.RECHAZADO)));
    }

    @PutMapping({"/users/{usuarioId}/validate", "/usuarios/{usuarioId}/validar"})
    @Operation(summary = "Aprobar pertenencia directamente por usuario (HU5)")
    public ResponseEntity<MensajeResponseDTO> aprobarValidacionUsuario(
            @PathVariable Long usuarioId,
            Authentication authentication) {
        String adminEmail = (authentication != null) ? authentication.getName() : null;
        adminService.aprobarValidacionPorUsuario(usuarioId, adminEmail);
        return ResponseEntity.ok(MensajeResponseDTO.of("Validación académica aprobada para el usuario", usuarioId, Map.of("estadoValidacion", EstadoValidacion.VALIDADO)));
    }
}
