package com.linkermak.cloud_file_storage.integration.authentication;

import com.linkermak.cloud_file_storage.integration.authentication.cleaner.SessionTestCleaner;
import com.linkermak.cloud_file_storage.integration.authentication.config.TestAuthBeansConfig;
import com.linkermak.cloud_file_storage.repositories.session.SessionRepository;
import com.linkermak.cloud_file_storage.repositories.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestAuthBeansConfig.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer(
                    DockerImageName.parse("postgres:16")
            );

    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected SessionRepository sessionRepository;

    @Autowired
    private SessionTestCleaner sessionCleaner;

    @Autowired
    protected ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                postgres::getJdbcUrl);
        registry.add("spring.datasource.username",
                postgres::getUsername);
        registry.add("spring.datasource.password",
                postgres::getPassword);
        registry.add("spring.datasource.driver-class-name",
                postgres::getDriverClassName);

        registry.add("spring.data.redis.host",
                redis::getHost);
        registry.add("spring.data.redis.port",
                () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        sessionCleaner.deleteAll();
    }

}
