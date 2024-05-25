package md.brainet.doeves.task;

import md.brainet.doeves.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TaskServiceImplIT extends IntegrationTestBase {

    @Autowired
    TaskServiceImpl taskService;

    @Test
    void fetchAllUserTasks() {
    }

    @Test
    void makeTask() {
    }

    @Test
    void editTask() {
    }

    @Test
    void deleteTask() {
    }

    @Test
    void changeStatus() {
    }
}