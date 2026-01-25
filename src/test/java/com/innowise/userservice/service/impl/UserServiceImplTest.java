package com.innowise.userservice.service.impl;

import com.innowise.userservice.exceptions.ResourceNotFoundException;
import com.innowise.userservice.exceptions.UserAlreadyExistsWithEmailException;
import com.innowise.userservice.mapper.PageResponseMapper;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.User;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.UserRequestDto;
import com.innowise.userservice.model.dto.UserResponseDto;
import com.innowise.userservice.model.dto.UserSearchCriteriaDto;
import com.innowise.userservice.model.dto.UserWithCardsDto;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;


@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private PageResponseMapper pageResponseMapper;
  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private UserServiceImpl userService;

  private User testUser;
  private UserResponseDto userResponseDto;
  private PageResponseDto<UserResponseDto> pageResponseDto;
  private UserWithCardsDto userWithCardsDto;
  private UserSearchCriteriaDto userSearchCriteriaDto;
  private UserRequestDto userRequestDto;
  private Pageable pageable;
  private Page<User> userPage;
  private LocalDateTime testDateTime;

  @BeforeEach
  void setUp() {
    testDateTime = LocalDateTime.of(2026, 1, 15, 10, 30);

    testUser = User.builder()
            .id(1L)
            .name("John")
            .surname("Doe")
            .email("john.doe@example.com")
            .birthDate(LocalDate.of(1990, 1, 15))
            .active(true)
            .paymentCards(Collections.emptyList())
            .build();
    testUser.setCreatedAt(testDateTime);
    testUser.setUpdatedAt(testDateTime);

    userRequestDto = UserRequestDto.builder()
            .name("John")
            .surname("Doe")
            .email("john.doe@example.com")
            .birthDate(LocalDate.of(1990, 1, 15))
            .active(true)
            .build();

    userResponseDto = UserResponseDto.builder()
            .id(1L)
            .name("John")
            .surname("Doe")
            .email("john.doe@example.com")
            .birthDate(LocalDate.of(1990, 1, 15))
            .active(true)
            .createdAt(testDateTime)
            .updatedAt(testDateTime)
            .build();

    userWithCardsDto = UserWithCardsDto.builder()
            .id(1L)
            .name("John")
            .surname("Doe")
            .email("john.doe@example.com")
            .birthDate(LocalDate.of(1990, 1, 15))
            .active(true)
            .createdAt(testDateTime)
            .updatedAt(testDateTime)
            .paymentCards(Collections.emptyList())
            .build();

    userSearchCriteriaDto = UserSearchCriteriaDto.builder()
            .name("John")
            .surname("Doe")
            .email("example.com")
            .active(true)
            .build();

    pageable = Pageable.ofSize(10).withPage(0);

    userPage = new org.springframework.data.domain.PageImpl<>(
            List.of(testUser),
            pageable,
            1
    );

    pageResponseDto = PageResponseDto.<UserResponseDto>builder()
            .content(List.of(userResponseDto))
            .currentPage(0)
            .pageSize(10)
            .totalElements(1L)
            .totalPages(1)
            .first(true)
            .last(true)
            .build();
  }


  @Nested
  @DisplayName("Create User Tests")
  class CreateUserTests{

    @Test
    @DisplayName("Should create user successfully when valid request and email does not exist")
    void shouldCreateUserSuccessfully(){

      when(userMapper.toEntity(userRequestDto)).thenReturn(testUser);
      when(userRepository.existsByEmail(userRequestDto.getEmail())).thenReturn(false);
      when(userRepository.save(any(User.class))).thenReturn(testUser);
      when(userMapper.toResponseDto(testUser)).thenReturn(userResponseDto);

      UserResponseDto testUserResponseDto = userService.createUser(userRequestDto);

      assertNotNull(testUserResponseDto);
      assertEquals(userResponseDto.getId(), testUserResponseDto.getId());
      assertEquals(userResponseDto.getEmail(), testUserResponseDto.getEmail());

      verify(userRepository).existsByEmail(userRequestDto.getEmail());
      verify(userMapper).toEntity(userRequestDto);
      verify(userRepository).save(testUser);
      verify(userMapper).toResponseDto(testUser);

    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsWithEmailException when email exists")
    void shouldThrowUserAlreadyExistsWithEmailException(){
      when(userMapper.toEntity(userRequestDto)).thenReturn(testUser);
      when(userRepository.existsByEmail(userRequestDto.getEmail())).thenReturn(true);

      final UserAlreadyExistsWithEmailException exception = assertThrows(
              UserAlreadyExistsWithEmailException.class,
              () -> userService.createUser(userRequestDto)
      );

      assertEquals(exception.getMessage(), "User with email " + userRequestDto.getEmail() + " already exists");
      verify(userMapper,times(1)).toEntity(userRequestDto);
      verify(userRepository,times(1)).existsByEmail(userRequestDto.getEmail());
      verify(userRepository, times(0)).save(any(User.class));
    }
  }

  @Nested
  @DisplayName("Find User Tests")
  class FindUserTests{

    @Test
    @DisplayName("Should find user by id successfully")
    void shouldFindUserByIdSuccessfully(){
      Long userId = 1L;
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(userMapper.toResponseDto(testUser)).thenReturn(userResponseDto);

      UserResponseDto userResponseDto = userService.findUserById(userId);

      assertNotNull(userResponseDto);
      assertEquals(userResponseDto.getId(), userId);
      assertEquals(userResponseDto.getEmail(), testUser.getEmail());
      verify(userRepository).findById(userId);
      verify(userMapper).toResponseDto(testUser);
    }

    @Test
    @DisplayName("Should find user by email successfully")
    void shouldFindUserByEmailSuccessfully(){
      String email = "john.doe@example.com";
      when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
      when(userMapper.toResponseDto(testUser)).thenReturn(userResponseDto);

      UserResponseDto userResponseDto = userService.findUserByEmail(email);

      assertNotNull(userResponseDto);
      assertEquals(userResponseDto.getId(), testUser.getId());
      assertEquals(userResponseDto.getEmail(), testUser.getEmail());
      verify(userRepository).findByEmail(email);
      verify(userMapper).toResponseDto(testUser);
    }

    @Test
    @DisplayName("Should find user with cards by user id successfully")
    void shouldFindUserWithCardsSuccessfully(){
      Long userId = 1L;
      when(userRepository.findByIdWithCards(userId)).thenReturn(Optional.of(testUser));
      when(userMapper.toWithCardsDto(testUser)).thenReturn(userWithCardsDto);

      UserWithCardsDto result = userService.findUserWithCardsByUserId(userId);

      assertNotNull(result);
      assertEquals(userWithCardsDto.getId(), result.getId());
      assertEquals(userWithCardsDto.getEmail(), result.getEmail());
      verify(userRepository).findByIdWithCards(userId);
      verify(userMapper).toWithCardsDto(testUser);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found by id")
    void shouldThrowExceptionWhenUserNotFoundById() {
      Long userId = 99L;
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
              () -> userService.findUserById(userId));
      assertEquals(exception.getMessage(), String.format("%s not found with %s: '%s'",
              "User", "id", userId));
      verify(userRepository).findById(userId);
      verify(userMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found by email")
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
      String email = "missing@example.com";
      when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
              () -> userService.findUserByEmail(email));
      assertEquals(exception.getMessage(), String.format("%s not found with %s: '%s'",
              "User", "email", email));
      verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user with cards not found")
    void shouldThrowExceptionWhenUserWithCardsNotFound() {
      Long userId = 99L;
      when(userRepository.findByIdWithCards(userId)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
              () -> userService.findUserWithCardsByUserId(userId));
      assertEquals(exception.getMessage(), String.format("%s not found with %s: '%s'",
              "User", "id", userId));
      verify(userRepository).findByIdWithCards(userId);
    }

  }

  @Nested
  @DisplayName("Find All Users Tests")
  class FindAllUsersTests{

    @Test
    @DisplayName("Should find all users with pagination successfully")
    void shouldFindAllUsersWithPaginationSuccessfully(){
      when(userRepository.findAll(pageable)).thenReturn(userPage);
      when(pageResponseMapper.mapToDto(
              eq(userPage),
              any(java.util.function.Function.class)
      )).thenReturn(pageResponseDto);


      PageResponseDto<UserResponseDto> result = userService.findAllUsers(pageable);

      assertNotNull(result);
      assertEquals(1, result.getContent().size());
      assertEquals(userResponseDto.getId(), result.getContent().get(0).getId());
      verify(userRepository).findAll(pageable);
      verify(pageResponseMapper).mapToDto(eq(userPage), any());
    }

    @Test
    @DisplayName("Should find all users successfully")
    void shouldFindAllUsersSuccessfully(){
      List<User> users = List.of(testUser);
      List<UserResponseDto> userResponseDtos = List.of(userResponseDto);
      when(userRepository.findAll()).thenReturn(users);
      when(userMapper.toResponseDtoList(users)).thenReturn(userResponseDtos);

      List<UserResponseDto> result = userService.findAllUsers();

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(userResponseDto.getId(), result.get(0).getId());
      verify(userRepository).findAll();
      verify(userMapper).toResponseDtoList(users);
    }

    @Test
    @DisplayName("Should find all active users successfully")
    void shouldFindAllActiveUsersSuccessfully(){
      List<User> activeUsers = List.of(testUser);
      List<UserResponseDto> activeUserResponseDtos = List.of(userResponseDto);
      when(userRepository.findAllActiveUsers()).thenReturn(activeUsers);
      when(userMapper.toResponseDtoList(activeUsers)).thenReturn(activeUserResponseDtos);

      List<UserResponseDto> result = userService.findAllActiveUsers();

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(userResponseDto.getId(), result.get(0).getId());
      verify(userRepository).findAllActiveUsers();
      verify(userMapper).toResponseDtoList(activeUsers);
    }

    @Test
    @DisplayName("Should find users by criteria successfully")
    void shouldFindUsersByCriteriaSuccessfully(){
      when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
      when(pageResponseMapper.mapToDto(eq(userPage), any(java.util.function.Function.class))).thenReturn(pageResponseDto);

      PageResponseDto<UserResponseDto> result = userService.findAllUsersByCriteria(userSearchCriteriaDto, pageable);

      assertNotNull(result);
      assertEquals(1, result.getContent().size());
      assertEquals(userResponseDto.getId(), result.getContent().get(0).getId());
      verify(userRepository).findAll(any(Specification.class), eq(pageable));
      verify(pageResponseMapper).mapToDto(eq(userPage), any());
    }


  }

  @Nested
  @DisplayName("Update User Tests")
  class UpdateUserTests{

    @Test
    @DisplayName("Should update user successfully when email is not changed")
    void shouldUpdateUserSuccessfullyWhenEmailNotChanged() {
      Long userId = 1L;
      UserRequestDto updateRequest = UserRequestDto.builder()
              .name("Jane")
              .surname("Smith")
              .email("john.doe@example.com")
              .birthDate(LocalDate.of(1992, 2, 2))
              .active(true)
              .build();


      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      verify(userRepository, never()).existsByEmail(any());
      when(userMapper.toResponseDto(testUser)).thenReturn(userResponseDto);

      UserResponseDto result = userService.updateUserById(updateRequest, userId);

      assertNotNull(result);
      verify(userRepository).findById(userId);
      verify(userMapper).updateEntityFromDto(updateRequest, testUser);
      verify(userMapper).toResponseDto(testUser);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsWithEmailException when updating to existing email")
    void shouldThrowExceptionWhenUpdatingToExistingEmail() {
      Long userId = 1L;
      UserRequestDto updateRequest = UserRequestDto.builder()
              .email("existing@example.com")
              .build();


      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(true);

      UserAlreadyExistsWithEmailException exception = assertThrows(
              UserAlreadyExistsWithEmailException.class,
              () -> userService.updateUserById(updateRequest, userId)
      );

      assertEquals("User with email " + updateRequest.getEmail() + " already exists",
              exception.getMessage());
      verify(userRepository).findById(userId);
      verify(userRepository).existsByEmail(updateRequest.getEmail());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existing user")
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {
      Long userId = 99L;
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
              () -> userService.updateUserById(userRequestDto, userId));
      assertEquals(exception.getMessage(), String.format("%s not found with %s: '%s'",
              "User", "id", userId));
      verify(userRepository).findById(userId);
    }

  }

  @Nested
  @DisplayName("Delete and Status Update Tests")
  class DeleteAndStatusUpdateTests{
    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully(){
      Long userId = 1L;
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      userService.deleteUserById(userId);

      verify(userRepository).findById(userId);
      verify(userRepository).delete(testUser);

    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existing user")
    void shouldThrowExceptionWhenDeletingNonExistingUser() {
      Long userId = 99L;
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
              () -> userService.deleteUserById(userId));
      assertEquals(exception.getMessage(), String.format("%s not found with %s: '%s'",
              "User", "id", userId));
      verify(userRepository).findById(userId);
      verify(userRepository, never()).delete((User) any());
    }


    @Test
    @DisplayName("Should update user active status successfully")
    void shouldUpdateUserActiveStatusSuccessfully(){
      Long userId = 1L;
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      userService.updateUserActiveStatusById(userId, false);
      verify(userRepository).findById(userId);
      assertEquals(false, testUser.isActive());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating status of non-existing user")
    void shouldThrowExceptionWhenUpdatingStatusForNonExistingUser() {
      Long userId = 99L;
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
              () -> userService.updateUserActiveStatusById(userId, false));
      assertEquals(exception.getMessage(), String.format("%s not found with %s: '%s'",
              "User", "id", userId));
      verify(userRepository).findById(userId);
    }

  }

  @Nested
  @DisplayName("Utility Methods Tests")
  class UtilityMethodsTests {

    @Test
    @DisplayName("Should check if user exists by email")
    void shouldCheckIfUserExistsByEmail() {
      String email = "john.doe@example.com";
      when(userRepository.existsByEmail(email)).thenReturn(true);

      boolean exists = userService.existsByEmail(email);

      assertEquals(true, exists);
      verify(userRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Should get user entity successfully")
    void shouldGetUserEntitySuccessfully() {

      when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

      User result = userService.getUserEntity(1L);

      assertNotNull(result);
      assertEquals(testUser.getId(), result.getId());
      verify(userRepository).findById(1L);
    }
  }
}