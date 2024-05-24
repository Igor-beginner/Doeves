package md.brainet.doeves.task;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;


// TODO create an extension and use this to reduce duplicates
@Test
@Sql(scripts = "classpath:data/init_test_data.sql")
public @interface DataTest {
}
