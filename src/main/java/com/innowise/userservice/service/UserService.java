package com.innowise.userservice.service;

import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  public boolean insertUser(User user) {
    if (userRepository.save(user) != null) {
      return true;
    };
    return false;
  }
}
