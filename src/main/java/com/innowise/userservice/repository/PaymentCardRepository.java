package com.innowise.userservice.repository;

import com.innowise.userservice.model.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

  @Query(value = "SELECT * FROM payment_cards WHERE user_id = :id", nativeQuery = true)
  List<PaymentCard> findPaymentCardsByUserId(@Param("id") long id);

  @Query("SELECT COUNT(pc) FROM PaymentCard pc WHERE pc.user.id = :userId")
  int countPaymentCardsByUserId(@Param("userId") Long userId);

}
