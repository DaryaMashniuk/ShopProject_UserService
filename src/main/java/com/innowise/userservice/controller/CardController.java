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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/cards")
public class CardController implements CardControllerApi {

  private final PaymentCardService paymentCardService;

  @Override
  @PostMapping
  public ResponseEntity<PaymentCardResponseDto> createCard(@RequestBody @Valid PaymentCardRequestDto paymentCardRequestDto) {
    PaymentCardResponseDto createdCard = paymentCardService.createPaymentCard(paymentCardRequestDto);
    return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
  }

  @Override
  @GetMapping
  public ResponseEntity<PageResponseDto<PaymentCardResponseDto>> getAllPaymentCards(
          @RequestParam(required = false) String number,
          @RequestParam(required = false) String holder,
          @RequestParam(required = false) Boolean active,
          Pageable pageable) {
    CardSearchCriteriaDto cardSearchCriteriaDto = new CardSearchCriteriaDto(number,holder,active);
    return ResponseEntity.ok(paymentCardService.findAllPaymentCards(cardSearchCriteriaDto,pageable));
  }

  @Override
  @GetMapping("/{id}")
  public ResponseEntity<PaymentCardResponseDto> getCardById(@PathVariable Long id) {
    return ResponseEntity.ok(paymentCardService.findPaymentCardById(id));
  }

  @Override
  @PutMapping("/{id}")
  public ResponseEntity<PaymentCardResponseDto> updateCard(@PathVariable Long id, @RequestBody @Valid PaymentCardRequestDto paymentCardRequestDto) {
    return ResponseEntity.ok(paymentCardService.updatePaymentCardById(paymentCardRequestDto, id));
  }

  @Override
  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateCardStatus(@PathVariable("id") Long id, @RequestBody @Valid ChangeStatusRequestDto statusDto) {
    paymentCardService.updatePaymentCardStatusById(id, statusDto.getActive());
    return ResponseEntity.ok().build();
  }

  @Override
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCardById(@PathVariable Long id) {
    paymentCardService.deletePaymentCardById(id);
    return ResponseEntity.noContent().build();
  }
}
