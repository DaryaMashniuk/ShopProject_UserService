package com.innowise.userservice.controller;

import com.innowise.userservice.constants.ApiConstants;
import com.innowise.userservice.controller.api.CardControllerApi;
import com.innowise.userservice.model.dto.CardSearchCriteriaDto;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.PaymentCardRequestDto;
import com.innowise.userservice.model.dto.PaymentCardResponseDto;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping(ApiConstants.CARDS)
public class CardController implements CardControllerApi {

  private PaymentCardService paymentCardService;
  private static final Logger logger = LogManager.getLogger(CardController.class);

  @Override
  public ResponseEntity<PaymentCardResponseDto> createCard(@RequestBody @Valid PaymentCardRequestDto paymentCardRequestDto) {
    logger.info("Creating new payment card for user ID: {}", paymentCardRequestDto.getUserId());
    PaymentCardResponseDto createdCard = paymentCardService.createPaymentCard(paymentCardRequestDto);
    return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<PageResponseDto<PaymentCardResponseDto>> getAllPaymentCards(Pageable pageable) {
    logger.info("Retrieving all payment cards with pagination - page: {}, size: {}, sort: {}",
            pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    return ResponseEntity.ok(paymentCardService.findAllPaymentCards(pageable));
  }

  @Override
  public ResponseEntity<PaymentCardResponseDto> getCardById(@PathVariable Long id) {
    logger.info("Retrieving payment card by ID: {}", id);
    return ResponseEntity.ok(paymentCardService.findPaymentCardById(id));
  }

  @Override
  public ResponseEntity<PaymentCardResponseDto> updateCard(@PathVariable Long id, @RequestBody @Valid PaymentCardRequestDto paymentCardRequestDto) {
    logger.info("Updating payment card with ID: {}", id);
    return ResponseEntity.ok(paymentCardService.updatePaymentCardById(paymentCardRequestDto, id));
  }

  @Override
  public ResponseEntity<Void> updateCardActivate(@PathVariable("id") Long id) {
    logger.info("Activating payment card with ID: {}", id);
    paymentCardService.updatePaymentCardStatusById(id, true);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> updateCardDeactivate(@PathVariable("id") Long id) {
    logger.info("Deactivating payment card with ID: {}", id);
    paymentCardService.updatePaymentCardStatusById(id, false);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<PageResponseDto<PaymentCardResponseDto>> getPaymentCardsByCriteria(
          Pageable pageable,
          @RequestBody @Valid CardSearchCriteriaDto searchCriteria
  ) {
    logger.info("Searching payment cards with criteria: {} and pagination - page: {}, size: {}",
            searchCriteria, pageable.getPageNumber(), pageable.getPageSize());
    return ResponseEntity.ok(paymentCardService.findAllCardsByCriteria(searchCriteria, pageable));
  }

  @Override
  public ResponseEntity<Void> deleteCardById(@PathVariable Long id) {
    logger.info("Deleting payment card with ID: {}", id);
    paymentCardService.deletePaymentCardById(id);
    return ResponseEntity.noContent().build();
  }
}
