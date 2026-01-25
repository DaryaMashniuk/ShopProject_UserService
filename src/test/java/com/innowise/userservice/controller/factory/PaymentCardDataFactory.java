package com.innowise.userservice.controller.factory;

import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PaymentCardDataFactory {

  private final PaymentCardRepository paymentCardRepository;

  public PaymentCardDataFactory(PaymentCardRepository paymentCardRepository) {
    this.paymentCardRepository = paymentCardRepository;
  }

  public PaymentCard createRandomCardForUser(User user) {
    String unique = java.util.UUID.randomUUID().toString().substring(0, 8);
    String cardNumber = "4" + String.format("%015d", System.currentTimeMillis() % 1000000000000000L);

    PaymentCard card = PaymentCard.builder()
            .number(cardNumber)
            .holder("HOLDER_" + unique)
            .expirationDate(LocalDate.now().plusYears(2))
            .active(true)
            .user(user)
            .build();

    return paymentCardRepository.saveAndFlush(card);
  }
}