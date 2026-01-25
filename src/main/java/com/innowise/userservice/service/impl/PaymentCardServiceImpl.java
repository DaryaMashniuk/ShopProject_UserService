package com.innowise.userservice.service.impl;

import com.innowise.userservice.exceptions.MaxCardAmountLimitException;
import com.innowise.userservice.exceptions.ResourceNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.mapper.PageResponseMapper;
import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import com.innowise.userservice.model.dto.CardSearchCriteriaDto;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.PaymentCardRequestDto;
import com.innowise.userservice.model.dto.PaymentCardResponseDto;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserCacheService;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specifications.CardSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
public class PaymentCardServiceImpl implements PaymentCardService {

  private final PaymentCardRepository paymentCardRepository;
  private final PaymentCardMapper paymentCardMapper;
  private final PageResponseMapper pageResponseMapper;
  private static final Logger logger = LogManager.getLogger(PaymentCardServiceImpl.class);
  private final UserService userService;
  private final UserCacheService userCacheService;
  private static final int MAX_CARD_AMOUNT = 5;

  @Override
  public PaymentCardResponseDto createPaymentCard(PaymentCardRequestDto paymentCardRequestDto) {
    long userId = paymentCardRequestDto.getUserId();
    User user = userService.getUserEntity(userId);
    int amountOfCards = paymentCardRepository.countPaymentCardsByUserId(userId);
    if (amountOfCards >= MAX_CARD_AMOUNT)
    {
      throw new MaxCardAmountLimitException(userId, amountOfCards , MAX_CARD_AMOUNT);
    }
    PaymentCard card = paymentCardMapper.toEntity(paymentCardRequestDto);
    card.setUser(user);
    PaymentCard saved = paymentCardRepository.save(card);
    userCacheService.evictUserCacheWithCards(userId);
    return paymentCardMapper.toResponseDto(saved);

  }

  @Transactional(readOnly = true)
  @Override
  public PaymentCardResponseDto findPaymentCardById(long id) {
    return paymentCardRepository
            .findById(id)
            .map(paymentCardMapper::toResponseDto)
           .orElseThrow(() -> new ResourceNotFoundException("Card", "id",id));
  }

  @Transactional(readOnly = true)
  @Override
  public List<PaymentCardResponseDto> findAllPaymentCards() {
    return paymentCardMapper.toResponseDtoList(paymentCardRepository.findAll());
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponseDto<PaymentCardResponseDto> findAllPaymentCards(Pageable pageable) {
    Page<PaymentCard> cards = paymentCardRepository.findAll(pageable);
    return pageResponseMapper.mapToDto(cards, paymentCardMapper::toResponseDto);
  }

  @Transactional
  @Override
  public PaymentCardResponseDto updatePaymentCardById(PaymentCardRequestDto paymentCardRequestDto, long id) {
    PaymentCard newCard = paymentCardRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Card", "id",id));

    paymentCardMapper.updateEntityFromDto(paymentCardRequestDto, newCard);
    userCacheService.evictUserCacheWithCards(newCard.getUser().getId());
    return paymentCardMapper.toResponseDto(newCard);
  }

  @Transactional
  @Override
  public void deletePaymentCardById(long id) {
    PaymentCard card = paymentCardRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Card", "id",id));
    logger.info("Delete payment card with id ={}",id);
    paymentCardRepository.delete(card);
    userCacheService.evictUserCacheWithCards(card.getUser().getId());
  }

  @Transactional
  @Override
  public void updatePaymentCardStatusById(long id, boolean status) {
    PaymentCard card = paymentCardRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Card", "id",id));
    logger.info("Update payment card with id = {} ",id);
    card.setActive(status);
    userCacheService.evictUserCacheWithCards(card.getUser().getId());
  }

  @Transactional(readOnly = true)
  @Override
  public List<PaymentCardResponseDto> findUsersPaymentCardsById(long id) {
    return paymentCardMapper.toResponseDtoList(paymentCardRepository.findPaymentCardsByUserId(id));
  }

  @Override
  public PageResponseDto<PaymentCardResponseDto> findAllCardsByCriteria(CardSearchCriteriaDto searchCriteria, Pageable pageable) {
    Specification<PaymentCard> spec = CardSpecification.build(searchCriteria);
    Page<PaymentCard> cards = paymentCardRepository.findAll(spec, pageable);
    return pageResponseMapper.mapToDto(cards, paymentCardMapper::toResponseDto);
  }
}
