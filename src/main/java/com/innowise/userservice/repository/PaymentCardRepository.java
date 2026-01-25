package com.innowise.userservice.repository;

import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

  @Query("SELECT card FROM PaymentCard card WHERE card.user.id=:id")
  List<PaymentCard> findPaymentCardsByUserId(@Param("id") long id);

  @Modifying
  @Query("UPDATE PaymentCard c SET c.active=:active WHERE c.id=:id")
  void updateStatusById(@Param("id") long id,@Param("active") boolean active);

  @Query("SELECT COUNT(pc) FROM PaymentCard pc WHERE pc.user.id = :userId")
  int countPaymentCardsByUserId(@Param("userId") Long userId);

  Long user(User user);
}
