package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.CardSearchCriteriaDto;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.PaymentCardRequestDto;
import com.innowise.userservice.model.dto.PaymentCardResponseDto;
import org.springframework.data.domain.Pageable;
import java.util.List;


public interface PaymentCardService {
  /**
   * Creates a new payment card
   */
  PaymentCardResponseDto createPaymentCard(PaymentCardRequestDto paymentCardRequestDto);

  /**
   * Finds a payment card by ID
   */
  PaymentCardResponseDto findPaymentCardById(long id);

  /**
   * Finds all payment cards without pagination
   */
  List<PaymentCardResponseDto> findAllPaymentCards();

  /**
   * Finds all payment cards with pagination
   */
  PageResponseDto<PaymentCardResponseDto> findAllPaymentCards(Pageable pageable);

  /**
   * Updates a payment card by ID
   */
  PaymentCardResponseDto updatePaymentCardById(PaymentCardRequestDto paymentCardRequestDto, long id);

  /**
   * Deletes a payment card by ID
   */
  void deletePaymentCardById(long id);

  /**
   * Updates payment card's active status
   */
  void updatePaymentCardStatusById(long id, boolean status);

  /**
   * Finds all payment cards for a specific user
   */
  List<PaymentCardResponseDto> findUsersPaymentCardsById(long id);

  /**
   * Searches payment cards by criteria with pagination
   */
  PageResponseDto<PaymentCardResponseDto> findAllCardsByCriteria(CardSearchCriteriaDto searchCriteria, Pageable pageable);
}