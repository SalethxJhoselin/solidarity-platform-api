package bo.edu.uagrm.backend.controllers;

import bo.edu.uagrm.backend.dtos.*;
import bo.edu.uagrm.backend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping({"/recover", "/recovery"})
    public ResponseEntity<java.util.Map<String, String>> recoverPassword(@Valid @RequestBody RecoveryRequestDTO request) {
        String token = authService.recoverPassword(request);
        // Devolvemos el token solo por motivos de desarrollo/Sprint 1
        return ResponseEntity.ok(java.util.Map.of(
            "mensaje", "Token de recuperación generado (simulación de email): " + token,
            "resetToken", token,
            "token", token
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<java.util.Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(java.util.Map.of(
            "mensaje", "Contraseña restablecida correctamente"
        ));
    }
}
