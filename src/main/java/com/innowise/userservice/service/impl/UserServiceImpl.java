package com.innowise.userservice.service.impl;

import com.innowise.userservice.exceptions.UserAlreadyWithEmailException;
import com.innowise.userservice.model.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specifications.UserSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

  @Override
  public User createUser(User user) {
      if (existsByEmail(user.getEmail())) {
        throw new UserAlreadyWithEmailException("User with email " + user.getEmail() + " already exists");
      }
      return userRepository.save(user);
  }

  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Override
  public User findUserById(long id) {

    return userRepository
            .findById(id)
            .orElseThrow(() -> new UserAlreadyWithEmailException("User not found with id = "+id));
  }

  @Override
  public User findUserByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public Page<User> findAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  @Override
  public List<User> findAllUsers() {
    return userRepository.findAll();
  }

  @Override
  public List<User> findAllActiveUsers() {
    return userRepository.findAllActiveUsers();
  }

  @Transactional
  @Override
  public User updateUserById(User user) {
    User newUser = findUserById(user.getId());
      if (user.getName() != null) {
        newUser.setName(user.getName());
      }
      if (user.getSurname() != null) {
        newUser.setSurname(user.getSurname());
      }
      if (user.getEmail() != null) {
        newUser.setEmail(user.getEmail());
      }
      if (user.getBirthDate() != null) {
        newUser.setBirthDate(user.getBirthDate());
      }
    return userRepository.save(newUser);
  }

  @Transactional
  @Override
  public void deleteUserById(long id) {
    findUserById(id);
    logger.info("Deleting user with id ={}",id);
    userRepository.deleteById(id);

  }

  @Transactional
  @Override
  public void updateUserActiveStatusById(long id,boolean status) {
    findUserById(id);
    logger.info("Updating user active status with id ={}",id);
    userRepository.updateActiveStatusById(id,status);
  }

  @Override
  public Page<User> findAllUsersByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
    Specification<User> spec = Specification.where((Specification<User>) null);
    if (StringUtils.hasLength(searchCriteria.get("name"))){
      spec = spec.and(UserSpecification.containsFirstNameCaseInsensitive(searchCriteria.get("name")));
    }

    if (StringUtils.hasLength(searchCriteria.get("surname"))){
      spec = spec.and(UserSpecification.containsSurnameCaseInsensitive(searchCriteria.get("surname")));
    }

    if (StringUtils.hasLength(searchCriteria.get("email"))){
      spec = spec.and(UserSpecification.containsEmailCaseInsensitive(searchCriteria.get("email")));
    }
    return userRepository.findAll(spec, pageable);
  }

  @Override
  public int countPaymentCardsByUserId(long id) {
    return userRepository.countPaymentCardsByUserId(id);
  }

}
