package com.innowise.userservice.service;

import com.innowise.userservice.model.User;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.UserRequestDto;
import com.innowise.userservice.model.dto.UserResponseDto;
import com.innowise.userservice.model.dto.UserSearchCriteriaDto;
import com.innowise.userservice.model.dto.UserWithCardsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface UserService {
  UserResponseDto createUser(UserRequestDto userRequestDto);
  boolean existsByEmail(String email);
  UserResponseDto findUserById(long id);
  UserResponseDto findUserByEmail(String email);
  PageResponseDto<UserResponseDto> findAllUsers(Pageable pageable);
  List<UserResponseDto> findAllUsers();
  List<UserResponseDto> findAllActiveUsers();
  UserResponseDto updateUserById(UserRequestDto userRequestDto, long id);
  void deleteUserById(long id);
  void updateUserActiveStatusById(long id,boolean status);
  PageResponseDto<UserResponseDto> findAllUsersByCriteria(UserSearchCriteriaDto userSearchCriteriaDto, Pageable pageable);
  UserWithCardsDto findUserWithCardsByUserId(long id);
  User getUserEntity(Long id);
}
