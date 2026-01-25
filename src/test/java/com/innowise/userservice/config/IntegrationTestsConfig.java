package com.innowise.userservice.config;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@Configuration
public class IntegrationTestsConfig {
  public static final GenericContainer<?> REDIS =
          new GenericContainer<>("redis:7")
                  .withExposedPorts(6379);

  static {
    REDIS.start();
  }

}
