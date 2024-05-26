package md.brainet.doeves.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NewTaskRequestMapperTest {

    NewTaskRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NewTaskRequestMapper();
    }

    @Test
    void apply() {
        //given
        var request = new NewTaskRequest(
                "some name",
                null,
                null
        );

        //when
        var task = mapper.apply(request);

        //then
        assertEquals(request.name(), task.getName());
    }
}