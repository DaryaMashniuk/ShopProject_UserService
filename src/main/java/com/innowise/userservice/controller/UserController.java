package com.innowise.userservice.controller;

import com.innowise.userservice.controller.api.UserControllerApi;
import com.innowise.userservice.model.dto.ChangeStatusRequestDto;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.UserRequestDto;
import com.innowise.userservice.model.dto.UserResponseDto;
import com.innowise.userservice.model.dto.UserSearchCriteriaDto;
import com.innowise.userservice.model.dto.UserWithCardsDto;
import com.innowise.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserControllerApi {

  private final UserService userService;

  @Override
  @PostMapping
  public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto userRequestDto) {
    UserResponseDto createdUser = userService.createUser(userRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
  }

  @Override
  @GetMapping
  public ResponseEntity<PageResponseDto<UserResponseDto>> getUsers(
          @RequestParam(required = false) String name,
          @RequestParam(required = false) String surname,
          @RequestParam(required = false) String email,
          @RequestParam(required = false) Boolean active,
          Pageable pageable) {
    UserSearchCriteriaDto searchCriteria = new UserSearchCriteriaDto(name, surname, email, active);
    PageResponseDto<UserResponseDto> users = userService.findAllUsers(searchCriteria, pageable);
    return ResponseEntity.ok(users);
  }

  @Override
  @GetMapping("/{id}")
  public ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") Long id) {
    UserResponseDto user = userService.findUserById(id);
    return ResponseEntity.ok(user);
  }

  @Override
  @PutMapping("/{id}")
  public ResponseEntity<UserResponseDto> updateUser(@PathVariable("id") Long id,
                                                    @RequestBody @Valid UserRequestDto userRequestDto) {
    UserResponseDto updatedUser = userService.updateUserById(userRequestDto, id);
    return ResponseEntity.ok(updatedUser);
  }

  @Override
  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateUserStatus(@PathVariable("id") Long id, @RequestBody @Valid ChangeStatusRequestDto statusDto) {
    userService.updateUserActiveStatusById(id, statusDto.getActive());
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping("/{id}/cards")
  public ResponseEntity<UserWithCardsDto> getUserWithCards(@PathVariable("id") Long id) {
    UserWithCardsDto user = userService.findUserWithCardsByUserId(id);
    return ResponseEntity.ok(user);
  }



  @Override
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
    userService.deleteUserById(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}