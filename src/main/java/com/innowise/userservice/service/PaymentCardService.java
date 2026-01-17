package com.innowise.userservice.service;
import com.innowise.userservice.model.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface PaymentCardService {
  PaymentCard createPaymentCard(PaymentCard paymentCard);
  PaymentCard getPaymentCardById(long id);
  List<PaymentCard> getAllPaymentCards();
  Page<PaymentCard> getAllPaymentCards(Pageable pageable);
  PaymentCard updatePaymentCardById(PaymentCard paymentCard);
  void deletePaymentCardById(long id);
  void updatePaymentCardStatusById(long id, boolean status);
  List<PaymentCard> getUsersPaymentCardsById(long id);
  Page<PaymentCard> findAllCardsByCriteria(Map<String, String> searchCriteria, Pageable pageable);
}
