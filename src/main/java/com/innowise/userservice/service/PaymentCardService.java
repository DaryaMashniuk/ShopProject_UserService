package com.innowise.userservice.service;
import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.dto.CardSearchCriteriaDto;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.PaymentCardRequestDto;
import com.innowise.userservice.model.dto.PaymentCardResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface PaymentCardService {
  PaymentCardResponseDto createPaymentCard(PaymentCardRequestDto paymentCardRequestDto);
  PaymentCardResponseDto findPaymentCardById(long id);
  List<PaymentCardResponseDto> findAllPaymentCards();
  PageResponseDto<PaymentCardResponseDto> findAllPaymentCards(Pageable pageable);
  PaymentCardResponseDto updatePaymentCardById(PaymentCardRequestDto paymentCardRequestDto, long id);
  void deletePaymentCardById(long id);
  void updatePaymentCardStatusById(long id, boolean status);
  List<PaymentCardResponseDto> findUsersPaymentCardsById(long id);
  PageResponseDto<PaymentCardResponseDto> findAllCardsByCriteria(CardSearchCriteriaDto searchCriteria, Pageable pageable);
}

