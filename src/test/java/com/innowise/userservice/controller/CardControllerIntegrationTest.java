package com.innowise.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.BaseIntegrationTest;
import com.innowise.userservice.controller.factory.PaymentCardDataFactory;
import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import com.innowise.userservice.model.dto.CardSearchCriteriaDto;
import com.innowise.userservice.model.dto.ChangeStatusRequestDto;
import com.innowise.userservice.model.dto.PaymentCardRequestDto;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.AuthorisationService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
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
class CardControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private PaymentCardRepository paymentCardRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PaymentCardDataFactory paymentCardDataFactory;

  @Autowired
  private CacheManager cacheManager;

  @MockBean(name = "authorisationService")
  private AuthorisationService authorisationService;

  private PaymentCard testCard;
  private User testUser;


  private void initTestData() {
    testUser = userRepository.save(User.builder()
            .name("John")
            .surname("Doe")
            .email("john.doe@example.com")
            .birthDate(LocalDate.of(1990, 1, 1))
            .active(true)
            .build());

    testCard = paymentCardDataFactory.createRandomCardForUser(testUser);

    when(authorisationService.hasAdminRole(any()))
            .thenReturn(true);
  }

  private Cache getUsersCache() {
    return cacheManager.getCache("users");
  }

  private Cache getUsersWithCardsCache() {
    return cacheManager.getCache("users-with-cards");
  }

  private void assertUserNotInCache(Long userId) {
    Cache cache = getUsersCache();
    assertThat(cache).isNotNull();
    assertThat(cache.get(userId)).isNull();
  }

  private void assertUserWithCardsNotInCache(Long userId) {
    Cache cache = getUsersWithCardsCache();
    assertThat(cache).isNotNull();
    assertThat(cache.get(userId)).isNull();
  }

  @Nested
  @DisplayName("Create Card Tests")
  class CreateCardTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create card successfully with valid request")
    void shouldCreateCardSuccessfully() throws Exception {
      initTestData();
      PaymentCardRequestDto newCardRequest = PaymentCardRequestDto.builder()
              .number("5111111111111111")
              .holder("JOHN SMITH")
              .expirationDate(LocalDate.now().plusYears(3))
              .active(true)
              .userId(testUser.getId())
              .build();

      mockMvc.perform(post("/api/v1/cards")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(newCardRequest)))
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.id").isNumber())
              .andExpect(jsonPath("$.number").value("5111111111111111"))
              .andExpect(jsonPath("$.holder").value("JOHN SMITH"))
              .andExpect(jsonPath("$.active").value(true))
              .andExpect(jsonPath("$.userId").value(testUser.getId()));

      assertUserWithCardsNotInCache(testUser.getId());
      assertUserNotInCache(testUser.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when creating card with invalid data")
    void shouldReturnBadRequestWhenInvalidData() throws Exception {
      initTestData();
      PaymentCardRequestDto invalidCard = PaymentCardRequestDto.builder()
              .number("123")
              .holder("A")
              .expirationDate(LocalDate.now().minusDays(1))
              .userId(testUser.getId())
              .build();

      mockMvc.perform(post("/api/v1/cards")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(invalidCard)))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.status").value(400))
              .andExpect(jsonPath("$.details.number").value("Card number must be 16 digits"))
              .andExpect(jsonPath("$.details.holder").value("Holder name must be between 2 and 100 characters"))
              .andExpect(jsonPath("$.details.expirationDate").value("Expiration date must be in the future"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when creating card for non-existent user")
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
      PaymentCardRequestDto cardRequest = PaymentCardRequestDto.builder()
              .number("4111111111111111")
              .holder("JOHN DOE")
              .expirationDate(LocalDate.now().plusYears(2))
              .active(true)
              .userId(999L)
              .build();

      mockMvc.perform(post("/api/v1/cards")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(cardRequest)))
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to create card")
    void shouldReturnForbiddenWhenRegularUserCreatesCard() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      PaymentCardRequestDto newCardRequest = PaymentCardRequestDto.builder()
              .number("5111111111111111")
              .holder("JOHN SMITH")
              .expirationDate(LocalDate.now().plusYears(3))
              .active(true)
              .userId(testUser.getId())
              .build();

      mockMvc.perform(post("/api/v1/cards")
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(newCardRequest)))
              .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Get Card Tests")
  class GetCardTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get card by id successfully")
    void shouldGetCardByIdSuccessfully() throws Exception {
      initTestData();

      mockMvc.perform(get("/api/v1/cards/{id}", testCard.getId()))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testCard.getId()))
              .andExpect(jsonPath("$.number").value(testCard.getNumber()))
              .andExpect(jsonPath("$.holder").value(testCard.getHolder()))
              .andExpect(jsonPath("$.active").value(true))
              .andExpect(jsonPath("$.userId").value(testUser.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when card not found")
    void shouldReturnNotFoundWhenCardDoesNotExist() throws Exception {
      Long nonExistentId = 999L;

      mockMvc.perform(get("/api/v1/cards/{id}", nonExistentId))
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Should get own card successfully")
    void shouldGetOwnCardSuccessfully() throws Exception {
      initTestData();
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelfCard(eq(testCard.getId()), any())).thenReturn(true);

      mockMvc.perform(get("/api/v1/cards/{id}", testCard.getId())
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testCard.getId()));
    }

    @Test
    @DisplayName("Should return 403 when getting other user's card")
    void shouldReturnForbiddenWhenGettingOtherUserCard() throws Exception {
      initTestData();
      when(authorisationService.hasAdminRole(any())).thenReturn(false);
      when(authorisationService.isSelfCard(eq(testCard.getId()), any())).thenReturn(false);

      mockMvc.perform(get("/api/v1/cards/{id}", testCard.getId())
                      .with(user(String.valueOf(testCard.getUser().getId()+1)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Get All Cards Tests")
  class GetAllCardsTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get all cards with pagination successfully")
    void shouldGetAllCardsWithPagination() throws Exception {
      User user1 = userRepository.save(User.builder()
              .name("User1")
              .surname("Test1")
              .email("user1@test.com")
              .birthDate(LocalDate.of(1990, 1, 1))
              .active(true)
              .build());

      paymentCardDataFactory.createRandomCardForUser(user1);
      paymentCardDataFactory.createRandomCardForUser(user1);

      mockMvc.perform(get("/api/v1/cards")
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content", hasSize(2)))
              .andExpect(jsonPath("$.currentPage").value(0))
              .andExpect(jsonPath("$.pageSize").value(10))
              .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get empty list when no cards exist")
    void shouldGetEmptyListWhenNoCardsExist() throws Exception {
      mockMvc.perform(get("/api/v1/cards")
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content", hasSize(0)))
              .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to get all cards")
    void shouldReturnForbiddenWhenRegularUserGetsAllCards() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(get("/api/v1/cards")
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Update Card Tests")
  class UpdateCardTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update card successfully")
    void shouldUpdateCardSuccessfully() throws Exception {
      initTestData();

      PaymentCardRequestDto updateRequest = PaymentCardRequestDto.builder()
              .number("5111111111111112")
              .holder("UPDATED HOLDER")
              .expirationDate(LocalDate.now().plusYears(4))
              .active(false)
              .userId(testUser.getId())
              .build();

      mockMvc.perform(put("/api/v1/cards/{id}", testCard.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(testCard.getId()))
              .andExpect(jsonPath("$.holder").value("UPDATED HOLDER"))
              .andExpect(jsonPath("$.active").value(false));

      assertUserWithCardsNotInCache(testUser.getId());
      assertUserNotInCache(testUser.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should activate card successfully")
    void shouldActivateCardSuccessfully() throws Exception {
      initTestData();
      testCard.setActive(false);
      paymentCardRepository.save(testCard);

      ChangeStatusRequestDto activateRequest = ChangeStatusRequestDto.builder()
              .active(true)
              .build();

      mockMvc.perform(patch("/api/v1/cards/{id}", testCard.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(activateRequest)))
              .andExpect(status().isOk());

      mockMvc.perform(get("/api/v1/cards/{id}", testCard.getId()))
              .andExpect(jsonPath("$.active").value(true));

      assertUserWithCardsNotInCache(testUser.getId());
      assertUserNotInCache(testUser.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should deactivate card successfully")
    void shouldDeactivateCardSuccessfully() throws Exception {
      initTestData();

      ChangeStatusRequestDto deactivateRequest = ChangeStatusRequestDto.builder()
              .active(false)
              .build();

      mockMvc.perform(patch("/api/v1/cards/{id}", testCard.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(deactivateRequest)))
              .andExpect(status().isOk());

      mockMvc.perform(get("/api/v1/cards/{id}", testCard.getId()))
              .andExpect(jsonPath("$.active").value(false));

      assertUserWithCardsNotInCache(testUser.getId());
      assertUserNotInCache(testUser.getId());


    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when status is null")
    void shouldReturnBadRequestWhenStatusIsNull() throws Exception {
      initTestData();

      ChangeStatusRequestDto nullStatusRequest = ChangeStatusRequestDto.builder()
              .active(null)
              .build();

      mockMvc.perform(patch("/api/v1/cards/{id}", testCard.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(nullStatusRequest)))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.status").value(400))
              .andExpect(jsonPath("$.details.active").value("must not be null"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when updating non-existent card")
    void shouldReturnNotFoundWhenUpdatingNonExistentCard() throws Exception {
      initTestData();
      PaymentCardRequestDto updateRequest = PaymentCardRequestDto.builder()
              .number("5111111111111111")
              .holder("UPDATED")
              .expirationDate(LocalDate.now().plusYears(2))
              .active(true)
              .userId(testUser.getId())
              .build();

      mockMvc.perform(put("/api/v1/cards/{id}", 999L)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when updating status of non-existent card")
    void shouldReturnNotFoundWhenUpdatingStatusOfNonExistentCard() throws Exception {
      ChangeStatusRequestDto activateRequest = ChangeStatusRequestDto.builder()
              .active(true)
              .build();

      mockMvc.perform(patch("/api/v1/cards/{id}", 999L)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(activateRequest)))
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to update card")
    void shouldReturnForbiddenWhenRegularUserUpdatesCard() throws Exception {
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      PaymentCardRequestDto updateRequest = PaymentCardRequestDto.builder()
              .number("5111111111111112")
              .holder("UPDATED HOLDER")
              .expirationDate(LocalDate.now().plusYears(4))
              .active(false)
              .userId(testUser.getId())
              .build();

      mockMvc.perform(put("/api/v1/cards/{id}", testCard.getId())
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(updateRequest)))
              .andExpect(status().isForbidden());
    }


  }

  @Nested
  @DisplayName("Search Cards Tests")
  class SearchCardsTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should search cards by criteria successfully")
    void shouldSearchCardsByCriteriaSuccessfully() throws Exception {
      User user = userRepository.save(User.builder()
              .name("Search")
              .surname("User")
              .email("search@test.com")
              .birthDate(LocalDate.of(1990, 1, 1))
              .active(true)
              .build());

      PaymentCard card = paymentCardDataFactory.createRandomCardForUser(user);

      CardSearchCriteriaDto searchCriteria = CardSearchCriteriaDto.builder()
              .holder(card.getHolder().substring(0, 4))
              .active(true)
              .build();

      mockMvc.perform(get("/api/v1/cards")
                      .param("holder", searchCriteria.getHolder())
                      .param("active", String.valueOf(searchCriteria.getActive()))
                      .param("page", "0")
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
              .andExpect(jsonPath("$.content[0].holder").value(card.getHolder()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return empty result when no cards match criteria")
    void shouldReturnEmptyResultWhenNoMatches() throws Exception {

      mockMvc.perform(get("/api/v1/cards")
                      .param("holder", "NONEXISTENT")
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
  @DisplayName("Delete Card Tests")
  class DeleteCardTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete card successfully")
    void shouldDeleteCardSuccessfully() throws Exception {
      initTestData();

      mockMvc.perform(delete("/api/v1/cards/{id}", testCard.getId()))
              .andExpect(status().isNoContent());

      mockMvc.perform(get("/api/v1/cards/{id}", testCard.getId()))
              .andExpect(status().isNotFound());

      assertUserWithCardsNotInCache(testUser.getId());
      assertUserNotInCache(testUser.getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when deleting non-existent card")
    void shouldReturnNotFoundWhenDeletingNonExistentCard() throws Exception {
      Long nonExistentId = 999L;

      mockMvc.perform(delete("/api/v1/cards/{id}", nonExistentId))
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Should return 403 when regular user tries to delete card")
    void shouldReturnForbiddenWhenRegularUserDeletesCard() throws Exception {
      initTestData();
      when(authorisationService.hasAdminRole(any())).thenReturn(false);

      mockMvc.perform(delete("/api/v1/cards/{id}", testCard.getId())
                      .with(user(String.valueOf(testUser.getId())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
              .andExpect(status().isForbidden());
    }
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("Should handle malformed JSON")
  void shouldHandleMalformedJson() throws Exception {
    String malformedJson = "{ \"number\": \"4111111111111111\", \"holder\": }";

    mockMvc.perform(post("/api/v1/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").isString());
  }
}