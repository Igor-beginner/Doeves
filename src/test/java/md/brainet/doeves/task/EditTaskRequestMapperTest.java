package md.brainet.doeves.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EditTaskRequestMapperTest {

    EditTaskRequestMapper editTaskRequestMapper;

    @BeforeEach
    void setUp() {
        editTaskRequestMapper = new EditTaskRequestMapper();
    }

    @Test
    void apply() {
        //given
        var name = "task";
        var description = "description";

        var request = new EditTaskRequest(
                Optional.of(name),
                Optional.of(description),
                Optional.empty()
        );

        //when
        var task = editTaskRequestMapper.apply(request);

        //then
        assertEquals(name, task.getName());
        assertEquals(description, task.getDescription());
        assertNull(task.getDeadline());
    }
}