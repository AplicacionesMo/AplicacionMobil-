package cl.duoc.tacticaldev.usuarios.controller;

import cl.duoc.tacticaldev.usuarios.dto.LoginRequest;
import cl.duoc.tacticaldev.usuarios.dto.UsuarioResponse;
import cl.duoc.tacticaldev.usuarios.exception.ApiException;
import cl.duoc.tacticaldev.usuarios.model.Rol;
import cl.duoc.tacticaldev.usuarios.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba la capa HTTP: rutas, codigos de respuesta y JSON. El servicio es un mock. */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UsuarioService service;

    @Test
    void loginCorrectoResponde200ConElRol() throws Exception {
        when(service.login(any(LoginRequest.class)))
                .thenReturn(new UsuarioResponse(1L, "Tecnico", "tecnico@tacticaldev.cl", Rol.TECNICO, true));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"tecnico@tacticaldev.cl\",\"clave\":\"tecnico123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("TECNICO"));
    }

    @Test
    void loginIncorrectoResponde401ConMensaje() throws Exception {
        when(service.login(any(LoginRequest.class)))
                .thenThrow(new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"tecnico@tacticaldev.cl\",\"clave\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }

    @Test
    void loginSinCorreoResponde400() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"\",\"clave\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
