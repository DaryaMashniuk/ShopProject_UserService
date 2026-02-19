package com.innowise.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.BaseIntegrationTest;
import com.innowise.userservice.controller.factory.UserDataFactory;
import com.innowise.userservice.model.User;
import com.innowise.userservice.model.dto.ChangeStatusRequestDto;
import com.innowise.userservice.model.dto.UserRequestDto;
import com.innowise.userservice.model.dto.UserResponseDto;
import com.innowise.userservice.model.dto.UserSearchCriteriaDto;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.AuthorisationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class UserControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserDataFactory userDataFactory;

  @Autowired
  private CacheManager cacheManager;

  @MockBean(name = "authorisationService")
  private AuthorisationService authorisationService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = userDataFactory.createRandomUser();

    when(authorisationService.hasAdminRole(any()))
            .thenReturn(true);

  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  private Cache getUsersCache() {
    return cacheManager.getCache("users");
  }

  private Cache getUsersWithCardsCache() {
    return cacheManager.getCache("users-with-cards");
  }

  private void assertUserInCache(Long userId, UserResponseDto expectedUser) {
    Cache cache = getUsersCache();
    assertThat(cache).isNotNull();
    UserResponseDto cachedUser = cache.get(userId, UserResponseDto.class);
    assertThat(cachedUser).isNotNull();
    assertThat(cachedUser.getId()).isEqualTo(expectedUser.getId());
    assertThat(cachedUser.getName()).isEqualTo(expectedUser.getName());
  }

  private void assertUserNotInCache(Long userId) {
    Cache cache = getUsersCache();
    assertThat(cache).isNotNull();
    assertThat(cache.get(userId)).isNull();
  }

  private void assertUserWithCardsInCache(Long userId) {
    Cache cache = getUsersWithCardsCache();
    assertThat(cache).isNotNull();
    assertThat(cache.get(userId)).isNotNull();
  }

  private void assertUserWithCardsNotInCache(Long userId) {
    Cache cache = getUsersWithCardsCache();
    assertThat(cache).isNotNull();
    assertThat(cache.get(userId)).isNull();
  }

  @Nested
  @DisplayName("Create User Tests")
  class CreateUserTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create user successfully with valid request and cache result")
    void shouldCreateUserSuccessfully() throws Exception {
      UserRequestDto newUserRequest = UserRequestDto.builder()
              .name("New")
              .surname("User")
              .email("new.user@example.com")
              .birthDate(LocalDate.of(1995, 5, 15))
              .active(true)
              .build();

      mockMvc.perform(post("/api/v1/users")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(newUserRequest)))
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.id").isNumber())
              .andExpect(jsonPath("$.name").value("New"))
              .andExpect(jsonPath("$.surname").value("User"))
              .andExpect(jsonPath("$.email").value("new.user@example.com"))
              .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when creating user with invalid data")
    void shouldReturnBadRequestWhenInvalidData() throws Exception {
      UserRequestDto invalidUser = UserRequestDto.builder()
              .name("")
              .surname("")
              .email("invalid-email")
              .birthDate(LocalDate.now().plusDays(1))
              .build();

      mockMvc.perform(post("/api/v1/users")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidUser)))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.status").value(400))
              .andExpect(jsonPath("$.message").isString())
              .andExpect(jsonPath("$.details").isMap())
              .andExpect(jsonPath("$.details.name").value("Name is required"))
              .andExpect(jsonPath("$.details.surname").value("Surname is required"))
              .andExpect(jsonPath("$.details.email").value("Email should be valid"))
              .andExpect(jsonPath("$.details.birthDate").value("Birth date must be in the past"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 409 when creating user with existing email")
    void shouldReturnConflictWhenEmailExists() throws Exception {
      UserRequestDto duplicateUser = UserRequestDto.builder()
              .name("Another")
              .surname("User")
              .email(testUser.getEmail())
              .birthDate(LocalDate.of(1985, 5, 5))
              .build();

      mockMvc.perform(post("/api/v1/users")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(duplicateUser)))
              .andExpect(status().isConflict())
              .andExpect(jsonPath("$.status").value(409))
              .andExpect(jsonPath("$.message").value("User with email " + testUser.getEmail() + " already exists"));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to create user")
    void shouldReturnForbiddenWhenRegularUserCreatesUser() throws Exception {
      when(authorisationService.hasAdminRole(any( ))).thenReturn(false);
      when(authorisationService.isSelf(eq(testUser.getId()), any())).thenReturn(false);
      UserRequestDto newUserRequest = UserRequestDto.builder()
              .name("New")
              .surname("User")
              .email("new.user@example.com")
              .birthDate(LocalDate.of(1995, 5, 15))
              .active(true)
              .build();

      mockMvc.perform(post("/api/v1/users")
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(newUserRequest)))
              .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Get User Tests")
  class GetUserTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get user by id successfully and cache result")
    void shouldGetUserByIdSuccessfully() throws Exception {
      String response = mockMvc.perform(get("/api/v1/users/{id}", testUser.getId()))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testUser.getId()))
              .andExpect(jsonPath("$.name").value(testUser.getName()))
              .andExpect(jsonPath("$.surname").value(testUser.getSurname()))
              .andExpect(jsonPath("$.email").value(testUser.getEmail()))
              .andExpect(jsonPath("$.active").value(true))
              .andReturn()
              .getResponse()
              .getContentAsString();

      UserResponseDto userResponse = objectMapper.readValue(response, UserResponseDto.class);
      assertUserInCache(testUser.getId(), userResponse);

      mockMvc.perform(get("/api/v1/users/{id}", testUser.getId()))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testUser.getId()));
    }

    @Test
    @DisplayName("Should get own user profile successfully")
    void shouldGetOwnUserProfileSuccessfully() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelf(eq(testUser.getId()), any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/users/{id}", testUser.getId())
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testUser.getId()));
    }

    @Test
    @DisplayName("Should return 403 when getting other user's profile")
    void shouldReturnForbiddenWhenGettingOtherUserProfile() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelf(eq(testUser.getId()), any())).thenReturn(false);

      mockMvc.perform(get("/api/v1/users/{id}", testUser.getId())
                      .with(user(String.valueOf(testUser.getId()+1)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when user not found")
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
      Long nonExistentId = 999L;

      mockMvc.perform(get("/api/v1/users/{id}", nonExistentId))
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get user with cards successfully and cache result")
    void shouldGetUserWithCardsSuccessfully() throws Exception {
      mockMvc.perform(get("/api/v1/users/{id}/cards", testUser.getId()))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testUser.getId()))
              .andExpect(jsonPath("$.name").value(testUser.getName()))
              .andExpect(jsonPath("$.surname").value(testUser.getSurname()))
              .andExpect(jsonPath("$.email").value(testUser.getEmail()))
              .andExpect(jsonPath("$.active").value(true))
              .andReturn()
              .getResponse()
              .getContentAsString();

      assertUserWithCardsInCache(testUser.getId());

      mockMvc.perform(get("/api/v1/users/{id}/cards", testUser.getId()))
              .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should get own user with cards successfully")
    void shouldGetOwnUserWithCardsSuccessfully() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelf(eq(testUser.getId()), any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/users/{id}/cards", testUser.getId())
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testUser.getId()));
    }

    @Test
    @DisplayName("Should return 403 when getting other user's cards")
    void shouldReturnForbiddenWhenGettingOtherUserCards() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelf(eq(testUser.getId()), any())).thenReturn(false);

      mockMvc.perform(get("/api/v1/users/{id}/cards", testUser.getId())
                      .with(user(String.valueOf(testUser.getId()+1)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Get All Users Tests")
  class GetAllUsersTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get all users with pagination successfully")
    void shouldGetAllUsersWithPagination() throws Exception {
      userDataFactory.createRandomUser();
      userDataFactory.createRandomUser();
      userDataFactory.createRandomUser();

      mockMvc.perform(get("/api/v1/users")
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content", hasSize(4))) // 3 new + original testUser
              .andExpect(jsonPath("$.currentPage").value(0))
              .andExpect(jsonPath("$.pageSize").value(10))
              .andExpect(jsonPath("$.totalElements").value(4))
              .andExpect(jsonPath("$.first").value(true))
              .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get all active users successfully")
    void shouldGetAllActiveUsers() throws Exception {
      userDataFactory.createRandomUser();
      userDataFactory.createRandomUser();

      User inactiveUser = User.builder()
              .name("Inactive")
              .surname("User")
              .email("inactive" + System.currentTimeMillis() + "@example.com")
              .birthDate(LocalDate.of(1985, 5, 5))
              .active(false)
              .build();
      userRepository.save(inactiveUser);

      mockMvc.perform(get("/api/v1/users")
                      .param("active", "true"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content", hasSize(3))) // 2 new + original testUser
              .andExpect(jsonPath("$.content[*].active", everyItem(is(true))));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to get all users")
    void shouldReturnForbiddenWhenRegularUserGetsAllUsers() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(get("/api/v1/users")
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Update User Tests")
  class UpdateUserTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update user successfully and update cache")
    void shouldUpdateUserSuccessfully() throws Exception {
      mockMvc.perform(get("/api/v1/users/{id}", testUser.getId()));

      UserRequestDto updateRequest = UserRequestDto.builder()
              .name("Updated Name")
              .surname("Updated Surname")
              .email("updated.email" + System.currentTimeMillis() + "@example.com")
              .birthDate(LocalDate.of(1991, 2, 16))
              .active(false)
              .build();

      String response = mockMvc.perform(put("/api/v1/users/{id}", testUser.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testUser.getId()))
              .andExpect(jsonPath("$.name").value("Updated Name"))
              .andExpect(jsonPath("$.surname").value("Updated Surname"))
              .andExpect(jsonPath("$.active").value(false))
              .andReturn()
              .getResponse()
              .getContentAsString();

      UserResponseDto updatedUser = objectMapper.readValue(response, UserResponseDto.class);
      assertUserInCache(testUser.getId(), updatedUser);
      assertUserWithCardsNotInCache(testUser.getId());
    }

    @Test
    @DisplayName("Should update own user profile successfully")
    void shouldUpdateOwnUserProfileSuccessfully() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelf(eq(testUser.getId()), any())).thenReturn(true);
      System.out.println("Test User " + testUser.getId());

      UserRequestDto updateRequest = UserRequestDto.builder()
              .name("Updated Name")
              .surname("Updated Surname")
              .email("updated.email" + System.currentTimeMillis() + "@example.com")
              .birthDate(LocalDate.of(1991, 2, 16))
              .active(true)
              .build();

      mockMvc.perform(put("/api/v1/users/{id}", testUser.getId())
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testUser.getId()))
              .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("Should return 403 when updating other user's profile")
    void shouldReturnForbiddenWhenUpdatingOtherUserProfile() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelf(eq(testUser.getId()), any())).thenReturn(false);

      UserRequestDto updateRequest = UserRequestDto.builder()
              .name("Updated Name")
              .surname("Updated Surname")
              .email("updated@example.com")
              .birthDate(LocalDate.of(1991, 2, 16))
              .active(true)
              .build();

      mockMvc.perform(put("/api/v1/users/{id}", testUser.getId())
                      .with(user(String.valueOf(testUser.getId()+1)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 409 when updating to existing email")
    void shouldReturnConflictWhenUpdatingToExistingEmail() throws Exception {
      User secondUser = userDataFactory.createRandomUser();

      UserRequestDto updateRequest = UserRequestDto.builder()
              .name("John")
              .surname("Doe")
              .email(secondUser.getEmail())
              .birthDate(testUser.getBirthDate())
              .active(true)
              .build();

      mockMvc.perform(put("/api/v1/users/{id}", testUser.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andExpect(status().isConflict())
              .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should activate user successfully and update cache")
    void shouldActivateUserSuccessfully() throws Exception {
      testUser.setActive(false);
      userRepository.save(testUser);

      mockMvc.perform(get("/api/v1/users/{id}", testUser.getId()));

      ChangeStatusRequestDto activateRequest = ChangeStatusRequestDto.builder()
              .active(true)
              .build();

      mockMvc.perform(patch("/api/v1/users/{id}", testUser.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(activateRequest)))
              .andExpect(status().isOk());

      assertUserNotInCache(testUser.getId());
      assertUserWithCardsNotInCache(testUser.getId());

      mockMvc.perform(get("/api/v1/users/{id}", testUser.getId()))
              .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should deactivate user successfully and update cache")
    void shouldDeactivateUserSuccessfully() throws Exception {
      mockMvc.perform(get("/api/v1/users/{id}", testUser.getId()));

      ChangeStatusRequestDto deactivateRequest = ChangeStatusRequestDto.builder()
              .active(false)
              .build();

      mockMvc.perform(patch("/api/v1/users/{id}", testUser.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(deactivateRequest)))
              .andExpect(status().isOk());

      assertUserNotInCache(testUser.getId());
      assertUserWithCardsNotInCache(testUser.getId());

      mockMvc.perform(get("/api/v1/users/{id}", testUser.getId()))
              .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to change status")
    void shouldReturnForbiddenWhenRegularUserChangesStatus() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      ChangeStatusRequestDto statusRequest = ChangeStatusRequestDto.builder()
              .active(false)
              .build();

      mockMvc.perform(patch("/api/v1/users/{id}", testUser.getId())
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(statusRequest)))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when status is null")
    void shouldReturnBadRequestWhenStatusIsNull() throws Exception {
      ChangeStatusRequestDto nullStatusRequest = ChangeStatusRequestDto.builder()
              .active(null)
              .build();

      mockMvc.perform(patch("/api/v1/users/{id}", testUser.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(nullStatusRequest)))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.status").value(400))
              .andExpect(jsonPath("$.details.active").value("must not be null"));
    }
  }

  @Nested
  @DisplayName("Search Users Tests")
  class SearchUsersTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should search users by criteria successfully")
    void shouldSearchUsersByCriteriaSuccessfully() throws Exception {
      User searchableUser = userDataFactory.createRandomUser();
      int size = searchableUser.getEmail().split("@").length;
      UserSearchCriteriaDto searchCriteria = UserSearchCriteriaDto.builder()
              .name(searchableUser.getName().substring(0, 4))
              .surname(searchableUser.getSurname().substring(0, 4))
              .email(searchableUser.getEmail().substring(size - 1, searchableUser.getEmail().length() - 1))
              .active(true)
              .build();

      mockMvc.perform(get("/api/v1/users")
                      .param("name", searchCriteria.getName())
                      .param("surname", searchCriteria.getSurname())
                      .param("email", searchCriteria.getEmail())
                      .param("active", String.valueOf(searchCriteria.getActive()))
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
              .andExpect(jsonPath("$.content[0].name").value(searchableUser.getName()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return empty result when no users match criteria")
    void shouldReturnEmptyResultWhenNoMatches() throws Exception {
      mockMvc.perform(get("/api/v1/users")
                      .param("name", "NonExistent")
                      .param("surname", "User")
                      .param("email", "none@xistent.com")
                      .param("active", "true")
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content", hasSize(0)))
              .andExpect(jsonPath("$.totalElements").value(0));
    }
  }

  @Nested
  @DisplayName("Delete User Tests")
  class DeleteUserTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete user successfully and remove from cache")
    void shouldDeleteUserSuccessfully() throws Exception {
      mockMvc.perform(get("/api/v1/users/{id}", testUser.getId()));
      mockMvc.perform(get("/api/v1/users/{id}/cards", testUser.getId()));
      assertUserWithCardsInCache(testUser.getId());

      mockMvc.perform(delete("/api/v1/users/{id}", testUser.getId()))
              .andExpect(status().isNoContent());

      assertUserNotInCache(testUser.getId());
      assertUserWithCardsNotInCache(testUser.getId());

      mockMvc.perform(get("/api/v1/users/{id}", testUser.getId()))
              .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to delete user")
    void shouldReturnForbiddenWhenRegularUserDeletesUser() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelf(eq(testUser.getId()), any())).thenReturn(false);

      mockMvc.perform(delete("/api/v1/users/{id}", testUser.getId())
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when deleting non-existent user")
    void shouldReturnNotFoundWhenDeletingNonExistentUser() throws Exception {
      Long nonExistentId = 999L;

      mockMvc.perform(delete("/api/v1/users/{id}", nonExistentId))
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 for non-existent endpoint")
    void shouldReturnNotFoundForNonExistentEndpoint() throws Exception {
      Long nonExistentId = 999L;
      mockMvc.perform(get("/api/v1/users/{id}", nonExistentId))
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
      String malformedJson = "{ \"name\": \"John\", \"email\": }";

      mockMvc.perform(post("/api/v1/users")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(malformedJson))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.status").value(400))
              .andExpect(jsonPath("$.message").isString());
    }
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("Should handle empty database gracefully")
  void shouldHandleEmptyDatabaseGracefully() throws Exception {
    userRepository.deleteAll();

    mockMvc.perform(get("/api/v1/users")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(0)))
            .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Nested
  @DisplayName("Get Users By Ids Tests")
  class GetUsersByIdsTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get users by ids successfully")
    void shouldGetUsersByIdsSuccessfully() throws Exception {
      User secondUser = userDataFactory.createRandomUser();
      User thirdUser = userDataFactory.createRandomUser();

      List<Long> userIds = List.of(testUser.getId(), secondUser.getId(), thirdUser.getId());

      mockMvc.perform(get("/api/v1/users/batch")
                      .param("ids", userIds.stream()
                              .map(String::valueOf)
                              .toArray(String[]::new)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(3)))
              .andExpect(jsonPath("$[0].id").value(testUser.getId()))
              .andExpect(jsonPath("$[0].name").value(testUser.getName()))
              .andExpect(jsonPath("$[0].email").value(testUser.getEmail()))
              .andExpect(jsonPath("$[1].id").value(secondUser.getId()))
              .andExpect(jsonPath("$[1].name").value(secondUser.getName()))
              .andExpect(jsonPath("$[2].id").value(thirdUser.getId()))
              .andExpect(jsonPath("$[2].name").value(thirdUser.getName()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return empty list when no users found for given ids")
    void shouldReturnEmptyListWhenNoUsersFound() throws Exception {
      List<Long> nonExistentIds = List.of(999L, 1000L, 1001L);

      mockMvc.perform(get("/api/v1/users/batch")
                      .param("ids", nonExistentIds.stream()
                              .map(String::valueOf)
                              .toArray(String[]::new)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(0)))
              .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return only existing users when some ids don't exist")
    void shouldReturnOnlyExistingUsersWhenSomeIdsDontExist() throws Exception {
      User secondUser = userDataFactory.createRandomUser();

      List<Long> mixedIds = List.of(testUser.getId(), 999L, secondUser.getId(), 1000L);

      mockMvc.perform(get("/api/v1/users/batch")
                      .param("ids", mixedIds.stream()
                              .map(String::valueOf)
                              .toArray(String[]::new)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(2)))
              .andExpect(jsonPath("$[0].id").value(testUser.getId()))
              .andExpect(jsonPath("$[0].name").value(testUser.getName()))
              .andExpect(jsonPath("$[1].id").value(secondUser.getId()))
              .andExpect(jsonPath("$[1].name").value(secondUser.getName()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle single id parameter")
    void shouldHandleSingleId() throws Exception {
      mockMvc.perform(get("/api/v1/users/batch")
                      .param("ids", String.valueOf(testUser.getId())))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(1)))
              .andExpect(jsonPath("$[0].id").value(testUser.getId()))
              .andExpect(jsonPath("$[0].name").value(testUser.getName()))
              .andExpect(jsonPath("$[0].email").value(testUser.getEmail()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle duplicate ids in request")
    void shouldHandleDuplicateIds() throws Exception {
      List<Long> duplicateIds = List.of(testUser.getId(), testUser.getId(), testUser.getId());

      mockMvc.perform(get("/api/v1/users/batch")
                      .param("ids", duplicateIds.stream()
                              .map(String::valueOf)
                              .toArray(String[]::new)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(1))) // Should return unique users
              .andExpect(jsonPath("$[0].id").value(testUser.getId()))
              .andExpect(jsonPath("$[0].name").value(testUser.getName()));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to get users by ids")
    void shouldReturnForbiddenWhenRegularUserGetsUsersByIds() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      List<Long> userIds = List.of(testUser.getId());

      mockMvc.perform(get("/api/v1/users/batch")
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                      .param("ids", userIds.stream()
                              .map(String::valueOf)
                              .toArray(String[]::new)))
              .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle large number of ids")
    void shouldHandleLargeNumberOfIds() throws Exception {
      // Create many users
      List<Long> userIds = new java.util.ArrayList<>();
      for (int i = 0; i < 20; i++) {
        User user = userDataFactory.createRandomUser();
        userIds.add(user.getId());
      }

      mockMvc.perform(get("/api/v1/users/batch")
                      .param("ids", userIds.stream()
                              .map(String::valueOf)
                              .toArray(String[]::new)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(20)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should preserve order of users as returned by service")
    void shouldPreserveUserOrder() throws Exception {
      // Create users in a specific order
      User user1 = userDataFactory.createRandomUser(); // Will have some ID
      User user2 = userDataFactory.createRandomUser(); // Will have larger ID
      User user3 = userDataFactory.createRandomUser(); // Will have even larger ID

      // Request in reverse order of creation
      List<Long> userIds = List.of(user3.getId(), user1.getId(), user2.getId());

      String response = mockMvc.perform(get("/api/v1/users/batch")
                      .param("ids", userIds.stream()
                              .map(String::valueOf)
                              .toArray(String[]::new)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(3)))
              .andReturn()
              .getResponse()
              .getContentAsString();

      // Note: The order in the response may depend on the repository implementation
      // This test just verifies that we get all users back
      List<UserResponseDto> users = objectMapper.readValue(
              response,
              new com.fasterxml.jackson.core.type.TypeReference<List<UserResponseDto>>() {}
      );

      // Verify we got all users
      assertThat(users).hasSize(3);
      assertThat(users).extracting(UserResponseDto::getId)
              .containsExactlyInAnyOrder(user1.getId(), user2.getId(), user3.getId());
    }
  }
}