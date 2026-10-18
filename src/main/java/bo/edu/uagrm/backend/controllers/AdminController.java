package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.RoleAssignRequestDTO;
import bo.edu.uagrm.backend.services.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/asignar-roles")
    public ResponseEntity<java.util.Map<String, Object>> asignarRoles(@Valid @RequestBody RoleAssignRequestDTO requestDTO) {
        adminService.assignRoles(requestDTO);
        return ResponseEntity.ok(java.util.Map.of(
            "mensaje", "Roles asignados correctamente al usuario ID: " + requestDTO.getUsuarioId(),
            "usuarioId", requestDTO.getUsuarioId(),
            "roles", requestDTO.getRoles()
        ));
    }
}
