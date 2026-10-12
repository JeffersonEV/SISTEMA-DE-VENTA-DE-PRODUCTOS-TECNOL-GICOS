package com.sistematecnologico;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de integracion: contexto completo + base H2 + seguridad JWT real.
 * Cubre: registro -> login -> token -> crear producto ADMIN (201), CLIENTE (403),
 * sin token (401), crear pedido, cancelar pedido y reportes por rol.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private tools.jackson.databind.ObjectMapper objectMapper;

    @Test
    @DisplayName("Flujo completo: auth + roles + pedidos + administrador")
    void flujoCompleto() throws Exception {
        // 1. Catalogo publico sin token
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray());

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 2. Acceso sin token a escritura -> 401
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        // 3. Registro de un cliente nuevo
        MvcResult registro = mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Integra","apellido":"Test","email":"integra@test.com","password":"Clave123*"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andReturn();

        String tokenCliente = json(registro).get("token").asText();

        // 4. Login del admin demo
        String tokenAdmin = login("admin@techstore.com", "Admin123*");

        // 5. El CLIENTE intenta crear un producto -> 403
        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"X","precio":10,"stock":1,"categoriaId":1}
                                """))
                .andExpect(status().isForbidden());

        // 6. El ADMIN crea un producto -> 201
        MvcResult crearProd = mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Laptop Test IT","descripcion":"demo",
                                 "precio":2500.00,"stock":8,"marca":"Demo","activo":true,"categoriaId":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        long productoId = json(crearProd).get("id").asLong();
        int stockInicial = json(crearProd).get("stock").asInt();

        // 7. El CLIENTE crea un pedido -> 201 y el total lo calcula el backend
        MvcResult crearPedido = mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productoId\":" + productoId + ",\"cantidad\":3}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(7500.00))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn();

        long pedidoId = json(crearPedido).get("id").asLong();

        // 8. El stock se desconto
        mockMvc.perform(get("/api/productos/" + productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(stockInicial - 3));

        // 9. El CLIENTE ve su historial
        mockMvc.perform(get("/api/pedidos/mis-pedidos")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pedidoId));

        // 10. Admin NO puede cancelar con token de cliente ajeno... el cliente cancela su pedido
        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));

        // 11. El stock se restauro
        mockMvc.perform(get("/api/productos/" + productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(stockInicial));

        // 12. CLIENTE sin acceso a panel admin -> 403
        mockMvc.perform(get("/api/admin/pedidos")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());

        // 13. ADMIN accede al panel y a reportes -> 200
        mockMvc.perform(get("/api/admin/pedidos")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray());

        mockMvc.perform(get("/api/admin/reportes/resumen")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVentas").isNumber());

        mockMvc.perform(get("/api/admin/reportes/top-productos")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray());

        // 14. Cliente sin token en endpoint protegido -> 401
        mockMvc.perform(get("/api/pedidos/mis-pedidos"))
                .andExpect(status().isUnauthorized());
    }

    private String login(String email, String password) throws Exception {
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return json(res).get("token").asText();
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}