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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
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
      User savedUser = userRepository.save(user);
      return userMapper.toResponseDto(savedUser);
  }

  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Cacheable(
          value = "users",
          key = "#id",
          unless = "#result == null"
  )
  @Transactional(readOnly = true)
  @Override
  public UserResponseDto findUserById(long id) {

    return userRepository
            .findById(id)
            .map(userMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
  }

  @Transactional(readOnly = true)
  @Override
  public UserResponseDto findUserByEmail(String email) {
    return userRepository
            .findByEmail(email)
            .map(userMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserResponseDto> findAllUsers() {
    return userMapper.toResponseDtoList(userRepository.findAll());
  }

  @CachePut(value = "users", key = "#id")
  @CacheEvict(value = "users-with-cards", key = "#id")
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

  @Caching(evict = {
          @CacheEvict(value = "users", key = "#id"),
          @CacheEvict(value = "users-with-cards", key = "#id"),
  })
  @Override
  public void deleteUserById(long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    logger.info("Deleting user with id ={}",id);
    userRepository.delete(user);
  }

  @Caching(evict = {
          @CacheEvict(value = "users", key = "#id"),
          @CacheEvict(value = "users-with-cards", key = "#id"),
})
  @Override
  public void updateUserActiveStatusById(long id,boolean status) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    user.setActive(status);
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponseDto<UserResponseDto> findAllUsers(UserSearchCriteriaDto searchCriteria, Pageable pageable) {
    boolean noFilters =
            searchCriteria.getName() == null &&
                    searchCriteria.getSurname() == null &&
                    searchCriteria.getEmail() == null &&
                    searchCriteria.getActive() == null;
    Page<User> users;
    if (noFilters) {
      users = userRepository.findAll(pageable);
    } else {
      Specification<User> spec = UserSpecification.build(searchCriteria);
      users = userRepository.findAll(spec, pageable);
    }

    return pageResponseMapper.mapToDto(users, userMapper::toResponseDto);
  }

  @Cacheable(
          value = "users-with-cards",
          key = "#id",
          unless = "#result == null"
  )
  @Transactional(readOnly = true)
  @Override
  public UserWithCardsDto findUserWithCardsByUserId(long id) {
    User user = userRepository.findByIdWithCards(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    return userMapper.toWithCardsDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public User getUserEntity(Long id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
  }

  public List<UserResponseDto> getUsersByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return userMapper.toResponseDtoList(userRepository.findAllById(ids));
  }
}
