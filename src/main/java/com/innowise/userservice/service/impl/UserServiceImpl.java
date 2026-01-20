package com.innowise.userservice.service.impl;

import com.innowise.userservice.exceptions.ResourceNotFoundException;
import com.innowise.userservice.exceptions.UserAlreadyExistsWithEmailException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.mapper.PageResponseMapper;
import com.innowise.userservice.model.User;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.UserRequestDto;
import com.innowise.userservice.model.dto.UserResponseDto;
import com.innowise.userservice.model.dto.UserSearchCriteriaDto;
import com.innowise.userservice.model.dto.UserWithCardsDto;
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
  private final UserMapper userMapper;
  private final PageResponseMapper pageResponseMapper;

  @Override
  public UserResponseDto createUser(UserRequestDto userRequestDto) {
    User user = userMapper.toEntity(userRequestDto);
      if (existsByEmail(user.getEmail())) {
        throw new UserAlreadyExistsWithEmailException("User with email " + user.getEmail() + " already exists");
      }
      return userMapper.toResponseDto(userRepository.save(user));
  }

  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Override
  public UserResponseDto findUserById(long id) {

    return userRepository
            .findById(id)
            .map(userMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
  }

  @Override
  public UserResponseDto findUserByEmail(String email) {
    return userRepository
            .findByEmail(email)
            .map(userMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
  }

  @Override
  public PageResponseDto<UserResponseDto> findAllUsers(Pageable pageable) {
    Page<User> users = userRepository.findAll(pageable);
    return pageResponseMapper.mapToDto(users, userMapper::toResponseDto);
  }

  @Override
  public List<UserResponseDto> findAllUsers() {
    return userMapper.toResponseDtoList(userRepository.findAll());
  }

  @Override
  public List<UserResponseDto> findAllActiveUsers() {
    return userMapper.toResponseDtoList(userRepository.findAllActiveUsers());
  }

  @Transactional
  @Override
  public UserResponseDto updateUserById(UserRequestDto userRequestDto, long id) {
    User newUser = userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

      if (userRequestDto.getEmail() != null &&
              !userRequestDto.getEmail().equals(newUser.getEmail()) &&
              existsByEmail(userRequestDto.getEmail())) {
          throw new UserAlreadyExistsWithEmailException("User with email " + userRequestDto.getEmail() + " already exists");
      }

    userMapper.updateEntityFromDto(userRequestDto, newUser);
    return userMapper.toResponseDto(newUser);
  }

  @Transactional
  @Override
  public void deleteUserById(long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    logger.info("Deleting user with id ={}",id);
    userRepository.delete(user);
  }

  @Transactional
  @Override
  public void updateUserActiveStatusById(long id,boolean status) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    logger.info("Updating user active status with id ={}",id);
    user.setActive(status);
  }

  @Override
  public PageResponseDto<UserResponseDto> findAllUsersByCriteria(UserSearchCriteriaDto searchCriteria, Pageable pageable) {
    Specification<User> spec = Specification.where((Specification<User>) null);
    if (StringUtils.hasLength(searchCriteria.getName())){
      spec = spec.and(UserSpecification.containsFirstNameCaseInsensitive(searchCriteria.getName()));
    }

    if (StringUtils.hasLength(searchCriteria.getSurname())){
      spec = spec.and(UserSpecification.containsSurnameCaseInsensitive(searchCriteria.getSurname()));
    }

    if (StringUtils.hasLength(searchCriteria.getEmail())){
      spec = spec.and(UserSpecification.containsEmailCaseInsensitive(searchCriteria.getEmail()));
    }

    if (searchCriteria.getActive() != null){
      spec = spec.and(UserSpecification.hasActiveStatus(searchCriteria.getActive()));
    }
    Page<User> users = userRepository.findAll(spec, pageable);
    return pageResponseMapper.mapToDto(users, userMapper::toResponseDto);
  }

  @Override
  public UserWithCardsDto findUserWithCardsByUserId(long id) {
    User user = userRepository.findByIdWithCards(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    return userMapper.toWithCardsDto(user);
  }


}
