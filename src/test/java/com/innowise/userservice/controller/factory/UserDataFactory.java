package com.innowise.userservice.controller.factory;

import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserDataFactory {

  private final UserRepository userRepository;
  private static long idCounter = 1;
  public UserDataFactory(UserRepository userRepository) {
    this.userRepository = userRepository;
  }


  public User createRandomUser() {
    String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
    List<PaymentCard> cards = new ArrayList<>();
    User user = User.builder()
            .id(idCounter++)
            .name("User" + unique)
            .surname("Surname" + unique)
            .email("user" + unique + "@test.com")
            .birthDate(LocalDate.of(1990, 1, 1))
            .active(true)
            .paymentCards(cards)
            .build();

    return userRepository.saveAndFlush(user);
  }
}