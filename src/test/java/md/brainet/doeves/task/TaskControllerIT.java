package md.brainet.doeves.task;

import md.brainet.doeves.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithUserDetails("test@mail.ru")
class TaskControllerIT extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TaskDao taskDao;

    @Test
    void fetchAll() throws Exception {
        mockMvc.perform(get("/api/v1/task/all"))
                .andExpect(content().json("""
                [
                    {
                        "id":1,
                        "name":"Task1",
                        "description":
                        "Description1",
                        "complete":false,
                        "dateOfCreate":"2024-05-26T21:43:19.229697",
                        "deadline":null
                    },
                    {
                        "id":2,
                        "name":"Task2",
                        "description":"Description2",
                        "complete":true,
                        "dateOfCreate":"2024-05-26T21:43:19.229697",
                        "deadline":null
                    },
                    {
                        "id":3,
                        "name":"Task3",
                        "description":"Description3",
                        "complete":false,
                        "dateOfCreate":"2024-05-26T21:43:19.229697",
                        "deadline":"2024-06-10T12:00:00.229697"
                    }
                ]
                """))
                .andExpect(status().isOk());
    }

    @Test
    void makeTask_taskValid_expect201() throws Exception {
        mockMvc.perform(post("/api/v1/task")
                        .content("""
                        {
                            "name" : "Task1",
                            "description" : "Description1",
                            "deadline" : null
                        }
                    """).contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isCreated());
    }

    @Test
    void makeTask_taskNotValid_expect400() throws Exception {
        mockMvc.perform(post("/api/v1/task")
                .content("""
                        {
                            "name" : ""
                        }
                    """).contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void edit_validTask_expect200() throws Exception {

        String newName = "otherTask";
        mockMvc.perform(patch("/api/v1/task/1")
                .content("""
                        {
                            "name" : "%s"
                        }
                    """.formatted(newName))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());

        var task = taskDao.selectById(1).get();
        assertEquals(newName, task.getName());
    }

    @Test
    void edit_notValidTask_expect400() throws Exception {
        String newName = " ";
        mockMvc.perform(patch("/api/v1/task/1")
                .content("""
                        {
                            "name" : "%s"
                        }
                    """.formatted(newName))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());

        var task = taskDao.selectById(1).get();
        assertNotEquals(newName, task.getName());
    }

    @Test
    void edit_strangerTask_expect403() throws Exception {
        String newName = "asdfasdf";
        mockMvc.perform(patch("/api/v1/task/404")
                .content("""
                        {
                            "name" : "%s"
                        }
                    """.formatted(newName))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    void edit_notExistsTask_expect403() throws Exception {
        String newName = "asdfasdf";
        mockMvc.perform(patch("/api/v1/task/4042")
                .content("""
                        {
                            "name" : "%s"
                        }
                    """.formatted(newName))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    void delete_validTask_expect200() throws Exception {
        mockMvc.perform(delete("/api/v1/task/1"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_strangerTask_expect403() throws Exception {
        mockMvc.perform(delete("/api/v1/task/404"))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_notExistTask_expect404() throws Exception {
        mockMvc.perform(delete("/api/v1/task/4053"))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeStatus_validTask_expect200() throws Exception {
        mockMvc.perform(
                    patch("/api/v1/task/1/status")
                            .param("complete", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk());

        var task = taskDao.selectById(1);
        assertTrue(task.get().isComplete());
    }

    @Test
    void changeStatus_strangerTask_expect403() throws Exception {
        mockMvc.perform(
                    patch("/api/v1/task/404/status")
                            .param("complete", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isForbidden());
    }

    @Test
    void changeStatus_notExistTask_expect404() throws Exception {
        mockMvc.perform(
                    patch("/api/v1/task/4053/status")
                            .param("complete", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(status().isNotFound());
    }
}