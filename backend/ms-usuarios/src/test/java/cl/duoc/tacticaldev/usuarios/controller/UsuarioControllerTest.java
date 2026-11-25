package cl.duoc.tacticaldev.usuarios.controller;

import cl.duoc.tacticaldev.usuarios.dto.UsuarioRequest;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Capa HTTP del CRUD de usuarios: rutas, codigos y la cabecera X-Rol. */
@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UsuarioService service;

    private static final String CUERPO = """
            {"nombre":"Camilo Godoy","correo":"camilo@tacticaldev.cl",
             "clave":"camilo123","rol":"TECNICO"}
            """;

    private UsuarioResponse respuesta() {
        return new UsuarioResponse(3L, "Camilo Godoy", "camilo@tacticaldev.cl", Rol.TECNICO, true);
    }

    @Test
    void listarDevuelveLosUsuarios() throws Exception {
        when(service.listar()).thenReturn(List.of(respuesta()));

        mvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("camilo@tacticaldev.cl"))
                .andExpect(jsonPath("$[0].rol").value("TECNICO"));
    }

    @Test
    void obtenerDevuelveUnUsuario() throws Exception {
        when(service.obtener(3L)).thenReturn(respuesta());

        mvc.perform(get("/api/v1/usuarios/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Camilo Godoy"));
    }

    @Test
    void obtenerInexistenteResponde404() throws Exception {
        when(service.obtener(99L)).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        mvc.perform(get("/api/v1/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Usuario no encontrado"));
    }

    @Test
    void crearComoAdminResponde201() throws Exception {
        when(service.crear(any(UsuarioRequest.class), eq("ADMIN"))).thenReturn(respuesta());

        mvc.perform(post("/api/v1/usuarios")
                        .header("X-Rol", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
    }

    /** Sin la cabecera X-Rol el controlador asume TECNICO y el servicio rechaza. */
    @Test
    void crearSinCabeceraDeRolResponde403() throws Exception {
        when(service.crear(any(UsuarioRequest.class), eq("TECNICO")))
                .thenThrow(new ApiException(HttpStatus.FORBIDDEN, "Solo el administrador gestiona usuarios"));

        mvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Solo el administrador gestiona usuarios"));
    }

    @Test
    void crearConDatosInvalidosResponde400() throws Exception {
        mvc.perform(post("/api/v1/usuarios")
                        .header("X-Rol", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"correo\":\"sin-arroba\",\"clave\":\"123\",\"rol\":\"TECNICO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarDevuelveElUsuarioEditado() throws Exception {
        when(service.actualizar(eq(3L), any(UsuarioRequest.class), eq("ADMIN"))).thenReturn(respuesta());

        mvc.perform(put("/api/v1/usuarios/3")
                        .header("X-Rol", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("camilo@tacticaldev.cl"));
    }

    @Test
    void eliminarResponde204() throws Exception {
        mvc.perform(delete("/api/v1/usuarios/3").header("X-Rol", "ADMIN"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(3L, "ADMIN");
    }

    @Test
    void eliminarSinSerAdminResponde403() throws Exception {
        doThrow(new ApiException(HttpStatus.FORBIDDEN, "Solo el administrador gestiona usuarios"))
                .when(service).eliminar(3L, "COORDINADOR");

        mvc.perform(delete("/api/v1/usuarios/3").header("X-Rol", "COORDINADOR"))
                .andExpect(status().isForbidden());
    }
}
