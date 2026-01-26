package com.innowise.userservice;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {

  static final PostgreSQLContainer<?> POSTGRES =
          new PostgreSQLContainer<>("postgres:15-alpine")
                  .withDatabaseName("testdb")
                  .withUsername("test")
                  .withPassword("test");

  static final GenericContainer<?> REDIS =
          new GenericContainer<>("redis:7")
                  .withExposedPorts(6379);

  static {
    POSTGRES.start();
    REDIS.start();
  }

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);

    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port",
            () -> REDIS.getMappedPort(6379));
  }
}


