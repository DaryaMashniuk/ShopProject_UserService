package com.innowise.userservice.service;

import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface UserService {
  User createUser(User user);
  boolean existsByEmail(String email);
  User findUserById(long id);
  User findUserByEmail(String email);
  Page<User> findAllUsers(Pageable pageable);
  List<User> findAllUsers();
  List<User> findAllActiveUsers();
  User updateUserById(User user);
  void deleteUserById(long id);
  void updateUserActiveStatusById(long id,boolean status);
  Page<User> findAllUsersByCriteria(Map<String,String> searchCriteria, Pageable pageable);
  int countPaymentCardsByUserId(long id);
}
