package cl.duoc.tacticaldev.usuarios.controller;

import cl.duoc.tacticaldev.usuarios.dto.LoginRequest;
import cl.duoc.tacticaldev.usuarios.dto.UsuarioResponse;
import cl.duoc.tacticaldev.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    /** POST /api/v1/auth/login  ->  200 con el usuario y su rol, o 401. */
    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }
}
