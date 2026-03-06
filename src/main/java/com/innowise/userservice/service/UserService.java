package com.innowise.userservice.service;

import com.innowise.userservice.model.User;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.UserRequestDto;
import com.innowise.userservice.model.dto.UserResponseDto;
import com.innowise.userservice.model.dto.UserSearchCriteriaDto;
import com.innowise.userservice.model.dto.UserWithCardsDto;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserService {
  /**
   * Creates a new user
   */
  UserResponseDto createUser(UserRequestDto userRequestDto);

  /**
   * Checks if a user exists by email
   */
  boolean existsByEmail(String email);

  /**
   * Finds a user by ID
   */
  UserResponseDto findUserById(long id);

  /**
   * Finds a user by email
   */
  UserResponseDto findUserByEmail(String email);

  /**
   * Finds all users without pagination
   */
  List<UserResponseDto> findAllUsers();

  /**
   * Updates a user by ID
   */
  UserResponseDto updateUserById(UserRequestDto userRequestDto, long id);

  /**
   * Deletes a user by ID
   */
  void deleteUserById(long id);

  /**
   * Updates user's active status
   */
  void updateUserActiveStatusById(long id, boolean status);

  /**
   * Searches users by criteria with pagination
   */
  PageResponseDto<UserResponseDto> findAllUsers(UserSearchCriteriaDto userSearchCriteriaDto, Pageable pageable);

  /**
   * Finds user with associated payment cards
   */
  UserWithCardsDto findUserWithCardsByUserId(long id);

  /**
   * Gets user entity by ID
   */
  User getUserEntity(Long id);

  List<UserResponseDto> getUsersByIds(List<Long> ids);
}