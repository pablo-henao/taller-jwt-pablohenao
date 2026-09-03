package com.empresa.helpdesk.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String registrarYObtenerToken(String endpoint, String username, String password) throws Exception {
        String json = String.format("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """, username, password);

        MvcResult result = mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseNode.get("token").asText();
    }

    private Long crearTicket(String token, String titulo, String descripcion, String prioridad) throws Exception {
        String ticketJson = String.format("""
                {
                    "titulo": "%s",
                    "descripcion": "%s",
                    "prioridad": "%s"
                }
                """, titulo, descripcion, prioridad);

        MvcResult result = mockMvc.perform(post("/api/v1/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketJson))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    @Test
    @DisplayName("1. Acceso a endpoints protegidos sin token debe retornar 403 Forbidden")
    void testAccesoProtegidoSinTokenRetorna403() throws Exception {
        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("2. Registro de usuario CLIENTE debe retornar 200 OK con token JWT")
    void testRegistroClienteExitoso() throws Exception {
        String registerJson = """
                {
                    "username": "cliente_registro_test",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("3. Login con credenciales válidas debe retornar 200 OK con token JWT")
    void testLoginExitoso() throws Exception {
        registrarYObtenerToken("/api/v1/auth/register", "cliente_login_valido", "password123");

        String loginJson = """
                {
                    "username": "cliente_login_valido",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("4. Login con contraseña incorrecta debe retornar 401 UNAUTHORIZED")
    void testLoginCredencialesInvalidasRetorna401() throws Exception {
        registrarYObtenerToken("/api/v1/auth/register", "cliente_bad_login", "password123");

        String badLoginJson = """
                {
                    "username": "cliente_bad_login",
                    "password": "password_incorrecto"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badLoginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("5. Rol CLIENTE puede crear tickets (201) y listarlos (200)")
    void testClientePuedeCrearYListarTickets() throws Exception {
        String clienteToken = registrarYObtenerToken("/api/v1/auth/register", "cliente_pedro", "password123");
        assertNotNull(clienteToken);

        Long ticketId = crearTicket(clienteToken, "Error en inicio de sesión", "La pantalla queda en blanco", "ALTA");
        assertNotNull(ticketId);

        mockMvc.perform(get("/api/v1/tickets")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Error en inicio de sesión"));
    }

    @Test
    @DisplayName("6. Rol CLIENTE no puede actualizar estado de ticket ni eliminarlo (403 Forbidden)")
    void testClienteNoPuedeActualizarEstadoNiEliminar() throws Exception {
        String clienteToken = registrarYObtenerToken("/api/v1/auth/register", "cliente_ana", "password123");
        Long ticketId = crearTicket(clienteToken, "Falla en reporte", "No descarga el PDF", "MEDIA");

        String updateEstadoJson = """
                {
                    "estado": "EN_PROCESO",
                    "tecnicoAsignado": "tech_soporte"
                }
                """;

        // Intentar actualizar estado -> 403 Forbidden
        mockMvc.perform(put("/api/v1/tickets/" + ticketId + "/estado")
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateEstadoJson))
                .andExpect(status().isForbidden());

        // Intentar eliminar ticket -> 403 Forbidden
        mockMvc.perform(delete("/api/v1/tickets/" + ticketId)
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("7. Rol SOPORTE puede actualizar estado de ticket (200), pero no eliminarlo (403)")
    void testSoportePuedeActualizarEstadoPeroNoEliminar() throws Exception {
        String clienteToken = registrarYObtenerToken("/api/v1/auth/register", "cliente_carlos", "password123");
        Long ticketId = crearTicket(clienteToken, "Pantalla congelada", "No responde el cursor", "CRITICA");

        String soporteToken = registrarYObtenerToken("/api/v1/auth/register-soporte", "soporte_laura", "password123");

        String updateEstadoJson = """
                {
                    "estado": "EN_PROCESO",
                    "tecnicoAsignado": "soporte_laura"
                }
                """;

        // SOPORTE actualiza estado -> 200 OK
        mockMvc.perform(put("/api/v1/tickets/" + ticketId + "/estado")
                        .header("Authorization", "Bearer " + soporteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateEstadoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"))
                .andExpect(jsonPath("$.tecnicoAsignado").value("soporte_laura"));

        // SOPORTE intenta eliminar -> 403 Forbidden
        mockMvc.perform(delete("/api/v1/tickets/" + ticketId)
                        .header("Authorization", "Bearer " + soporteToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("8. Rol ADMIN tiene acceso total para eliminar tickets (204) y consultar auditoría (200)")
    void testAdminTieneAccesoTotalYAuditoria() throws Exception {
        String clienteToken = registrarYObtenerToken("/api/v1/auth/register", "cliente_maria", "password123");
        Long ticketId = crearTicket(clienteToken, "Bug visual", "Margen incorrecto", "BAJA");

        String adminToken = registrarYObtenerToken("/api/v1/auth/register-admin", "admin_root", "admin123");

        // ADMIN consulta panel de auditoría -> 200 OK
        mockMvc.perform(get("/api/v1/admin/auditoria")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sistema").value("Helpdesk Enterprise Security API"))
                .andExpect(jsonPath("$.estado").value("OPERACIONAL"));

        // ADMIN elimina ticket -> 204 No Content
        mockMvc.perform(delete("/api/v1/tickets/" + ticketId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("9. Rol CLIENTE no puede acceder al endpoint de auditoría de ADMIN (403 Forbidden)")
    void testClienteNoPuedeAccederAuditoriaAdmin() throws Exception {
        String clienteToken = registrarYObtenerToken("/api/v1/auth/register", "cliente_diego", "password123");

        mockMvc.perform(get("/api/v1/admin/auditoria")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(status().isForbidden());
    }
}
