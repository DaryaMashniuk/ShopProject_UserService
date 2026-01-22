package com.innowise.userservice.controller;

import com.innowise.userservice.constants.ApiConstants;
import com.innowise.userservice.controller.api.UserControllerApi;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.UserRequestDto;
import com.innowise.userservice.model.dto.UserResponseDto;
import com.innowise.userservice.model.dto.UserSearchCriteriaDto;
import com.innowise.userservice.model.dto.UserWithCardsDto;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping(ApiConstants.USERS)
public class UserController implements UserControllerApi {

  private static final Logger logger = LogManager.getLogger(UserController.class);
  private UserService userService;

  @Override
  public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto userRequestDto) {
    logger.info("Creating new user with email: {}", userRequestDto.getEmail());
    UserResponseDto createdUser = userService.createUser(userRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }

  @Override
  public ResponseEntity<PageResponseDto<UserResponseDto>> getUsers(Pageable pageable) {
    logger.info("Retrieving all users with pagination - page: {}, size: {}",
            pageable.getPageNumber(), pageable.getPageSize());
    PageResponseDto<UserResponseDto> users = userService.findAllUsers(pageable);
    return ResponseEntity.ok(users);
  }

  @Override
  public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") Long id) {
    logger.info("Retrieving user by ID: {}", id);
    UserResponseDto user = userService.findUserById(id);
    return ResponseEntity.ok(user);
  }

  @Override
  public ResponseEntity<UserResponseDto> updateUser(@PathVariable("id") Long id,
                                                    @RequestBody @Valid UserRequestDto userRequestDto) {
    logger.info("Updating user with ID: {}", id);
    UserResponseDto updatedUser = userService.updateUserById(userRequestDto, id);
    return ResponseEntity.ok(updatedUser);
  }

  @Override
  public ResponseEntity<Void> updateUserActivate(@PathVariable("id") Long id) {
    logger.info("Activating user with ID: {}", id);
    userService.updateUserActiveStatusById(id, true);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> updateUserDeactivate(@PathVariable("id") Long id) {
    logger.info("Deactivating user with ID: {}", id);
    userService.updateUserActiveStatusById(id, false);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<UserWithCardsDto> getUserWithCards(@PathVariable("id") Long id) {
    logger.info("Retrieving user with cards by ID: {}", id);
    UserWithCardsDto user = userService.findUserWithCardsByUserId(id);
    return ResponseEntity.ok(user);
  }

  @Override
  public ResponseEntity<PageResponseDto<UserResponseDto>> getUsersByCriteria(
          Pageable pageable,
          @RequestBody @Valid UserSearchCriteriaDto searchCriteria
  ) {
    logger.info("Searching users with criteria: {} and pagination - page: {}, size: {}",
            searchCriteria, pageable.getPageNumber(), pageable.getPageSize());
    PageResponseDto<UserResponseDto> users = userService.findAllUsersByCriteria(searchCriteria, pageable);
    return ResponseEntity.ok(users);
  }

  @Override
  public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
    logger.info("Deleting user with ID: {}", id);
    userService.deleteUserById(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @Override
  public ResponseEntity<List<UserResponseDto>> getActiveUsers() {
    logger.info("Retrieving all active users");
    List<UserResponseDto> activeUsers = userService.findAllActiveUsers();
    return ResponseEntity.ok(activeUsers);
  }
}