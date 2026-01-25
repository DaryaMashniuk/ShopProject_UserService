package com.innowise.userservice.controller;

import com.innowise.userservice.controller.api.CardControllerApi;
import com.innowise.userservice.model.dto.CardSearchCriteriaDto;
import com.innowise.userservice.model.dto.ChangeStatusRequestDto;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.PaymentCardRequestDto;
import com.innowise.userservice.model.dto.PaymentCardResponseDto;
import com.innowise.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/cards")
public class CardController implements CardControllerApi {

  private PaymentCardService paymentCardService;
  private static final Logger logger = LogManager.getLogger(CardController.class);

  @Override
  @PostMapping
  public ResponseEntity<PaymentCardResponseDto> createCard(@RequestBody @Valid PaymentCardRequestDto paymentCardRequestDto) {
    logger.info("Creating new payment card for user ID: {}", paymentCardRequestDto.getUserId());
    PaymentCardResponseDto createdCard = paymentCardService.createPaymentCard(paymentCardRequestDto);
    return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
  }

  @Override
  @GetMapping
  public ResponseEntity<PageResponseDto<PaymentCardResponseDto>> getAllPaymentCards(Pageable pageable) {
    logger.info("Retrieving all payment cards with pagination - page: {}, size: {}, sort: {}",
            pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
    return ResponseEntity.ok(paymentCardService.findAllPaymentCards(pageable));
  }

  @Override
  @GetMapping("/{id}")
  public ResponseEntity<PaymentCardResponseDto> getCardById(@PathVariable Long id) {
    logger.info("Retrieving payment card by ID: {}", id);
    return ResponseEntity.ok(paymentCardService.findPaymentCardById(id));
  }

  @Override
  @PutMapping("/{id}")
  public ResponseEntity<PaymentCardResponseDto> updateCard(@PathVariable Long id, @RequestBody @Valid PaymentCardRequestDto paymentCardRequestDto) {
    logger.info("Updating payment card with ID: {}", id);
    return ResponseEntity.ok(paymentCardService.updatePaymentCardById(paymentCardRequestDto, id));
  }

  @Override
  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateCardStatus(@PathVariable("id") Long id, @RequestBody @Valid ChangeStatusRequestDto statusDto) {
    paymentCardService.updatePaymentCardStatusById(id, statusDto.getActive());
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping("/search")
  public ResponseEntity<PageResponseDto<PaymentCardResponseDto>> getPaymentCardsByCriteria(
          Pageable pageable,
          @RequestBody @Valid CardSearchCriteriaDto searchCriteria
  ) {
    logger.info("Searching payment cards with criteria: {} and pagination - page: {}, size: {}",
            searchCriteria, pageable.getPageNumber(), pageable.getPageSize());
    return ResponseEntity.ok(paymentCardService.findAllCardsByCriteria(searchCriteria, pageable));
  }

  @Override
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCardById(@PathVariable Long id) {
    logger.info("Deleting payment card with ID: {}", id);
    paymentCardService.deletePaymentCardById(id);
    return ResponseEntity.noContent().build();
  }
}
