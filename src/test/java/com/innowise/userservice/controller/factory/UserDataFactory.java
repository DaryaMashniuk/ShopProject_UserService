package com.innowise.userservice.controller.factory;

import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.UserRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class UserDataFactory {

  private final UserRepository userRepository;

  public UserDataFactory(UserRepository userRepository) {
    this.userRepository = userRepository;
  }


  public User createRandomUser() {
    String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
    User user = User.builder()
            .name("User" + unique)
            .surname("Surname" + unique)
            .email("user" + unique + "@test.com")
            .birthDate(LocalDate.of(1990, 1, 1))
            .active(true)
            .build();

    return userRepository.saveAndFlush(user);
  }
}