package md.brainet.doeves;


import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    private static final PostgreSQLContainer<?> CONTAINER
            = new PostgreSQLContainer<>(
                    "postgres:latest"
    );

    @BeforeAll
    static void beforeAll() {
        CONTAINER.start();
        migrateFlyway();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", CONTAINER::getJdbcUrl);
    }

    private static void migrateFlyway() {
        Flyway flyway = Flyway
                .configure()
                .dataSource(
                        CONTAINER.getJdbcUrl(),
                        CONTAINER.getUsername(),
                        CONTAINER.getPassword()
                ).load();
        flyway.migrate();
    }
}
