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

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray());

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

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

        String tokenAdmin = login("admin@techstore.com", "Admin123*");

        mockMvc.perform(post("/api/productos")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"X","precio":10,"stock":1,"categoriaId":1}
                                """))
                .andExpect(status().isForbidden());

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

        MvcResult crearPedido = mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productoId\":" + productoId + ",\"cantidad\":3}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(7500.00))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn();

        long pedidoId = json(crearPedido).get("id").asLong();

        mockMvc.perform(get("/api/productos/" + productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(stockInicial - 3));

        mockMvc.perform(get("/api/pedidos/mis-pedidos")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pedidoId));

        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/cancelar")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));

        mockMvc.perform(get("/api/productos/" + productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(stockInicial));

        mockMvc.perform(get("/api/admin/pedidos")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isForbidden());

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
