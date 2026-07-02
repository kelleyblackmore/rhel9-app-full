package com.kelleyblackmore.secureledger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.kelleyblackmore.secureledger.TestSupport.bearer;
import static com.kelleyblackmore.secureledger.TestSupport.login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskAndRbacTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullTaskCrudLifecycleAsUser() throws Exception {
        String token = login(mockMvc, objectMapper, "user", "user123");

        // CREATE
        MvcResult created = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Write tests\",\"description\":\"cover crud\",\"status\":\"TODO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.owner").value("user"))
                .andReturn();

        JsonNode node = objectMapper.readTree(created.getResponse().getContentAsString());
        long id = node.get("id").asLong();

        // READ
        mockMvc.perform(get("/api/tasks/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TODO"));

        // LIST (paged)
        mockMvc.perform(get("/api/tasks").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // UPDATE
        mockMvc.perform(put("/api/tasks/" + id)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Write tests\",\"description\":\"done\",\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        // DELETE
        mockMvc.perform(delete("/api/tasks/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        // Confirm gone
        mockMvc.perform(get("/api/tasks/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createFailsValidationWhenTitleBlank() throws Exception {
        String token = login(mockMvc, objectMapper, "user", "user123");
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void userCannotAccessAuditEndpoint() throws Exception {
        String token = login(mockMvc, objectMapper, "user", "user123");
        mockMvc.perform(get("/api/audit").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAuditEndpoint() throws Exception {
        String token = login(mockMvc, objectMapper, "admin", "admin123");
        mockMvc.perform(get("/api/audit").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void userCannotUpdateAnotherUsersTask() throws Exception {
        // admin creates a task owned by "admin"
        String adminToken = login(mockMvc, objectMapper, "admin", "admin123");
        MvcResult created = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"admin task\",\"description\":\"secret\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // regular user tries to update it -> forbidden (owner check in service)
        String userToken = login(mockMvc, objectMapper, "user", "user123");
        mockMvc.perform(put("/api/tasks/" + id)
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"hijack\",\"description\":\"x\"}"))
                .andExpect(status().isForbidden());
    }
}
