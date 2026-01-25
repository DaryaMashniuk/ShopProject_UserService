package com.innowise.userservice;

import com.innowise.userservice.config.IntegrationTestsConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

  @PersistenceContext
  protected EntityManager entityManager;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private RedisConnectionFactory redisConnectionFactory;

  @Container
  static PostgreSQLContainer<?> postgres =
          new PostgreSQLContainer<>("postgres:14")
                  .withDatabaseName("testdb")
                  .withUsername("test")
                  .withPassword("test");

  @DynamicPropertySource
  static void registerPgProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);


    registry.add("spring.data.redis.host", IntegrationTestsConfig.REDIS::getHost);
    registry.add("spring.data.redis.port", () -> IntegrationTestsConfig.REDIS.getMappedPort(6379));
  }

  @BeforeEach
  public void cleanup() {
    jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
    redisConnectionFactory.getConnection().serverCommands().flushAll();
  }

}

