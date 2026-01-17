package com.innowise.userservice.service.impl;

import com.innowise.userservice.exceptions.CardNotFoundException;
import com.innowise.userservice.exceptions.MaxCardAmountLimitException;
import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specifications.CardSpecification;
import com.innowise.userservice.specifications.UserSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PaymentCardServiceImpl implements PaymentCardService {

  private final PaymentCardRepository paymentCardRepository;
  private final UserService userService;
  private final Logger logger = LogManager.getLogger(PaymentCardServiceImpl.class);

  @Override
  public PaymentCard createPaymentCard(PaymentCard paymentCard) {
    long userId = paymentCard.getUser().getId();
    int amountOfCards = userService.countPaymentCardsByUserId(userId);
    if (amountOfCards >= 5)
    {
      throw new MaxCardAmountLimitException("User with id "+ userId+" already has 5 cards");
    }
    return paymentCardRepository.save(paymentCard);
  }

  @Override
  public PaymentCard getPaymentCardById(long id) {
    return paymentCardRepository
            .findById(id)
           .orElseThrow(() -> new CardNotFoundException("No card with id = "+ id));
  }

  @Override
  public List<PaymentCard> getAllPaymentCards() {
    return paymentCardRepository.findAll();
  }

  @Override
  public Page<PaymentCard> getAllPaymentCards(Pageable pageable) {
    return paymentCardRepository.findAll(pageable);
  }

  @Transactional
  @Override
  public PaymentCard updatePaymentCardById(PaymentCard paymentCard) {
    PaymentCard newCard = getPaymentCardById(paymentCard.getId());
    if (paymentCard.getNumber() != null) {
      newCard.setNumber(paymentCard.getNumber());
    }
    if (paymentCard.getExpirationDate() != null) {
      newCard.setExpirationDate(paymentCard.getExpirationDate());
    }

    if (paymentCard.getHolder() != null) {
      newCard.setHolder(paymentCard.getHolder());
    }
    return paymentCardRepository.save(newCard);
  }

  @Transactional
  @Override
  public void deletePaymentCardById(long id) {
    getPaymentCardById(id);
    logger.info("Delete payment card with id ={}",id);
    paymentCardRepository.deleteById(id);
  }

  @Transactional
  @Override
  public void updatePaymentCardStatusById(long id, boolean status) {
    getPaymentCardById(id);
    logger.info("Update payment card with id = {} ",id);
    paymentCardRepository.updateStatusById(id, status);
  }

  @Override
  public List<PaymentCard> getUsersPaymentCardsById(long id) {
    return paymentCardRepository.findPaymentCardsByUserId(id);
  }

  @Override
  public Page<PaymentCard> findAllCardsByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
    Specification<PaymentCard> spec = Specification.where((Specification<PaymentCard>) null);

    if (StringUtils.hasLength(searchCriteria.get("number"))){
      spec = spec.and(CardSpecification.hasNumber(searchCriteria.get("number")));
    }

    if (StringUtils.hasLength(searchCriteria.get("holder"))){
      spec = spec.and(CardSpecification.containsHolderCaseInsensitive(searchCriteria.get("holder")));
    }
    return paymentCardRepository.findAll(spec, pageable);
  }
}
