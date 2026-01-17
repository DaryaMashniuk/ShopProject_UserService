package com.innowise.userservice.repository;

import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  boolean existsByEmail(String email);
  User findByEmail(String email);

  @Modifying
  @Query("UPDATE User u SET u.active=:active WHERE u.id=:id")
  void updateActiveStatusById(@Param("id") long id, @Param("active") boolean active);

  @Query(value = "SELECT * FROM users WHERE active = true", nativeQuery = true)
  List<User> findAllActiveUsers();

  @Query("SELECT COUNT(pc) FROM PaymentCard pc WHERE pc.user.id = :userId")
  int countPaymentCardsByUserId(@Param("userId") Long userId);
}
