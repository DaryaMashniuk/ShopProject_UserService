package com.innowise.userservice.service.impl;

import com.innowise.userservice.exceptions.MaxCardAmountLimitException;
import com.innowise.userservice.exceptions.ResourceNotFoundException;
import com.innowise.userservice.mapper.PageResponseMapper;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import com.innowise.userservice.model.dto.CardSearchCriteriaDto;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.PaymentCardRequestDto;
import com.innowise.userservice.model.dto.PaymentCardResponseDto;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.service.UserCacheService;
import com.innowise.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;


@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCardServiceImpl Unit Tests")
class PaymentCardServiceImplTest {

  @Mock
  private PaymentCardRepository paymentCardRepository;
  @Mock
  private PaymentCardMapper paymentCardMapper;
  @Mock
  private PageResponseMapper pageResponseMapper;
  @Mock
  private UserService userService;
  @Mock
  private UserCacheService userCacheService;

  private PaymentCard testCard;
  private PaymentCardResponseDto cardResponseDto;
  private PaymentCardRequestDto cardRequestDto;
  private PageResponseDto<PaymentCardResponseDto> cardPageResponseDto;
  private User testUser;
  private CardSearchCriteriaDto cardSearchCriteriaDto;
  private Pageable pageable;
  private Page<PaymentCard> cardPage;
  private LocalDateTime testDateTime;

  @InjectMocks
  private PaymentCardServiceImpl paymentCardService;

  @BeforeEach
  void setUp() {
    testDateTime = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

    testUser = User.builder()
            .id(1L)
            .name("John")
            .surname("Doe")
            .email("john.doe@example.com")
            .active(true)
            .build();
    testUser.setCreatedAt(testDateTime);
    testUser.setUpdatedAt(testDateTime);

    testCard = PaymentCard.builder()
            .id(1L)
            .number("4111111111111111")
            .holder("JOHN DOE")
            .expirationDate(LocalDate.of(2027, 12, 31))
            .active(true)
            .user(testUser)
            .build();
    testCard.setCreatedAt(testDateTime);
    testCard.setUpdatedAt(testDateTime);

    cardResponseDto = PaymentCardResponseDto.builder()
            .id(1L)
            .number("411111******1111")
            .holder("JOHN DOE")
            .expirationDate(LocalDate.of(2027, 12, 31))
            .active(true)
            .userId(1L)
            .createdAt(testDateTime)
            .updatedAt(testDateTime)
            .build();

    cardRequestDto = PaymentCardRequestDto.builder()
            .number("4111111111111111")
            .holder("JOHN DOE")
            .expirationDate(LocalDate.of(2027, 12, 31))
            .active(true)
            .userId(1L)
            .build();

    cardSearchCriteriaDto = CardSearchCriteriaDto.builder()
            .number("411111")
            .holder("JOHN")
            .active(true)
            .build();
    pageable = PageRequest.of(0, 10);
    cardPage = new PageImpl<>(List.of(testCard), pageable, 1);

    cardPageResponseDto = PageResponseDto.<PaymentCardResponseDto>builder()
            .content(List.of(cardResponseDto))
            .currentPage(0)
            .pageSize(10)
            .totalElements(1L)
            .totalPages(1)
            .first(true)
            .last(true)
            .build();
  }

  @Nested
  @DisplayName("Create Payment Card Tests")
  class CreatePaymentCardTests {

    @Test
    @DisplayName("Should create payment card successfully when user has less than max cards")
    void shouldCreatePaymentCardSuccessfully() {
      Long userId = 1L;
      when(userService.getUserEntity(userId)).thenReturn(testUser);
      when(paymentCardRepository.countPaymentCardsByUserId(userId)).thenReturn(3);
      when(paymentCardMapper.toEntity(cardRequestDto)).thenReturn(testCard);
      when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(testCard);
      when(paymentCardMapper.toResponseDto(testCard)).thenReturn(cardResponseDto);

      PaymentCardResponseDto result = paymentCardService.createPaymentCard(cardRequestDto);

      assertNotNull(result);
      assertEquals(cardResponseDto.getId(), result.getId());
      assertEquals(cardResponseDto.getNumber(), result.getNumber());
      verify(userService).getUserEntity(userId);
      verify(paymentCardRepository).countPaymentCardsByUserId(userId);
      verify(paymentCardMapper).toEntity(cardRequestDto);
      verify(paymentCardRepository).save(testCard);
      verify(paymentCardMapper).toResponseDto(testCard);
      verify(userCacheService).evictUserCacheWithCards(userId);
    }

    @Test
    @DisplayName("Should throw MaxCardAmountLimitException when user has max cards")
    void shouldThrowMaxCardAmountLimitException() {
      Long userId = 1L;
      when(userService.getUserEntity(userId)).thenReturn(testUser);
      when(paymentCardRepository.countPaymentCardsByUserId(userId)).thenReturn(5);

      MaxCardAmountLimitException exception = assertThrows(
              MaxCardAmountLimitException.class,
              () -> paymentCardService.createPaymentCard(cardRequestDto)
      );
      assertEquals(exception.getMessage(),String.format(
              "User with id %d already has %d cards. Maximum allowed is %d cards.",
              userId, 5, 5));
      assertNotNull(exception);
      verify(userService).getUserEntity(userId);
      verify(paymentCardRepository).countPaymentCardsByUserId(userId);
      verify(paymentCardRepository, never()).save(any());
    }

  }

  @Nested
  @DisplayName("Find Payment Card Tests")
  class FindPaymentCardTests {

    @Test
    @DisplayName("Should find payment card by id successfully")
    void shouldFindPaymentCardSuccessfully() {
      Long id = 1L;
      when(paymentCardRepository.findById(id)).thenReturn(Optional.of(testCard));
      when(paymentCardMapper.toResponseDto(testCard)).thenReturn(cardResponseDto);

      PaymentCardResponseDto result = paymentCardService.findPaymentCardById(id);

      assertNotNull(result);
      assertEquals(cardResponseDto.getId(), result.getId());
      assertEquals(cardResponseDto.getNumber(), result.getNumber());
      verify(paymentCardRepository).findById(id);
      verify(paymentCardMapper).toResponseDto(testCard);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when card not found")
    void shouldThrowResourceNotFoundExceptionWhenCardNotFound() {
      Long id = 1L;
      when(paymentCardRepository.findById(id)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(
              ResourceNotFoundException.class,
              () -> paymentCardService.findPaymentCardById(id)
      );

      assertEquals(exception.getMessage(), String.format("%s not found with %s: '%s'",
              "Card", "id",id));
      verify(paymentCardRepository).findById(id);
    }
  }

  @Nested
  @DisplayName("Find All Payment Cards Tests")
  class FindAllPaymentCardsTests {

    @Test
    @DisplayName("Should find all payment cards without pagination successfully")
    void shouldFindAllPaymentCardsSuccessfully() {
      List<PaymentCard> cards = List.of(testCard);
      List<PaymentCardResponseDto> cardDtos = List.of(cardResponseDto);
      when(paymentCardRepository.findAll()).thenReturn(cards);
      when(paymentCardMapper.toResponseDtoList(cards)).thenReturn(cardDtos);

      List<PaymentCardResponseDto> result = paymentCardService.findAllPaymentCards();

      assertNotNull(result);
      assertEquals(cardDtos.size(), result.size());
      assertEquals(cardResponseDto.getId(), result.get(0).getId());
      verify(paymentCardRepository).findAll();
      verify(paymentCardMapper).toResponseDtoList(cards);

    }

    @Test
    @DisplayName("Should find all payment cards with pagination successfully")
    void shouldFindAllPaymentCardsWithPaginationSuccessfully() {
      when(paymentCardRepository.findAll(pageable)).thenReturn(cardPage);
      when(pageResponseMapper.mapToDto(
              eq(cardPage),
              any(java.util.function.Function.class)
      )).thenReturn(cardPageResponseDto);

      CardSearchCriteriaDto searchCriteriaDto = new CardSearchCriteriaDto();
      PageResponseDto<PaymentCardResponseDto> result = paymentCardService.findAllPaymentCards(searchCriteriaDto,pageable);

      assertNotNull(result);
      assertEquals(1, result.getContent().size());
      assertEquals(cardResponseDto.getId(), result.getContent().get(0).getId());
      verify(paymentCardRepository).findAll(pageable);
      verify(pageResponseMapper).mapToDto(eq(cardPage), any());
    }

    @Test
    @DisplayName("Should find user's payment cards successfully")
    void shouldFindUsersPaymentCardsSuccessfully() {
      Long userId = 1L;
      List<PaymentCard> userCards = List.of(testCard);
      List<PaymentCardResponseDto> userCardDtos = List.of(cardResponseDto);
      when(paymentCardRepository.findPaymentCardsByUserId(userId)).thenReturn(userCards);
      when(paymentCardMapper.toResponseDtoList(userCards)).thenReturn(userCardDtos);

      List<PaymentCardResponseDto> result = paymentCardService.findUsersPaymentCardsById(userId);

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(cardResponseDto.getId(), result.get(0).getId());
      verify(paymentCardRepository).findPaymentCardsByUserId(1L);
      verify(paymentCardMapper).toResponseDtoList(userCards);
    }

    @Test
    @DisplayName("Should find cards by criteria successfully")
    void shouldFindCardsByCriteriaSuccessfully() {
      when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);
      when(pageResponseMapper.mapToDto(
              eq(cardPage),
              any(java.util.function.Function.class)
      )).thenReturn(cardPageResponseDto);

      PageResponseDto<PaymentCardResponseDto> result =
              paymentCardService.findAllPaymentCards(cardSearchCriteriaDto, pageable);

      assertNotNull(result);
      assertEquals(1, result.getContent().size());
      assertEquals(cardResponseDto.getId(), result.getContent().get(0).getId());
      verify(paymentCardRepository).findAll(any(Specification.class), eq(pageable));
      verify(pageResponseMapper).mapToDto(eq(cardPage), any());
    }
  }

  @Nested
  @DisplayName("Update Payment Card Tests")
  class UpdatePaymentCardTests {

    @Test
    @DisplayName("Should update payment card successfully")
    void shouldUpdatePaymentCardSuccessfully() {
      Long id = 1L;
      PaymentCardRequestDto updateRequest = PaymentCardRequestDto.builder()
              .number("4222222222222222")
              .holder("JANE SMITH")
              .expirationDate(LocalDate.of(2026, 11, 30))
              .active(true)
              .userId(1L)
              .build();
      when(paymentCardRepository.findById(id)).thenReturn(Optional.of(testCard));
      when(paymentCardMapper.toResponseDto(testCard)).thenReturn(cardResponseDto);

      PaymentCardResponseDto result = paymentCardService.updatePaymentCardById(updateRequest, id);

      assertNotNull(result);
      verify(paymentCardRepository).findById(id);
      verify(paymentCardMapper).updateEntityFromDto(updateRequest, testCard);
      verify(paymentCardMapper).toResponseDto(testCard);
      verify(userCacheService).evictUserCacheWithCards(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existing card")
    void shouldThrowExceptionWhenUpdatingNonExistingCard() {
      Long id = 99L;
      when(paymentCardRepository.findById(id)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
              () -> paymentCardService.updatePaymentCardById(cardRequestDto, id));
      assertEquals(exception.getMessage(), String.format("%s not found with %s: '%s'",
              "Card", "id",id));
      verify(paymentCardRepository).findById(id);
      verify(paymentCardMapper, never()).updateEntityFromDto(any(), any());
    }

  }

  @Nested
  @DisplayName("Delete and Status Update Tests")
  class DeleteAndStatusUpdateTests {

    @Test
    @DisplayName("Should delete payment card successfully")
    void shouldDeletePaymentCardSuccessfully() {
      Long id = 1L;
      when(paymentCardRepository.findById(id)).thenReturn(Optional.of(testCard));

      paymentCardService.deletePaymentCardById(1L);

      verify(paymentCardRepository).findById(id);
      verify(paymentCardRepository).delete(testCard);
      verify(userCacheService).evictUserCacheWithCards(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existing card")
    void shouldThrowExceptionWhenDeletingNonExistingCard() {
      Long id = 99L;
      when(paymentCardRepository.findById(id)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
              () -> paymentCardService.deletePaymentCardById(id));
      assertEquals(exception.getMessage(), String.format("%s not found with %s: '%s'",
              "Card", "id",id));
      verify(paymentCardRepository).findById(id);
      verify(userCacheService, never()).evictUserCacheWithCards(any());
    }


    @Test
    @DisplayName("Should update payment card status successfully")
    void shouldUpdatePaymentCardStatusSuccessfully() {
      Long id = 1L;
      when(paymentCardRepository.findById(id)).thenReturn(Optional.of(testCard));

      paymentCardService.updatePaymentCardStatusById(id, false);

      verify(paymentCardRepository).findById(id);
      assertFalse(testCard.isActive());
      verify(userCacheService).evictUserCacheWithCards(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating status of a non-existing card")
    void shouldThrowExceptionWhenUpdatingStatusOfNonExistingCard() {
      Long id = 99L;
      when(paymentCardRepository.findById(id)).thenReturn(Optional.empty());

      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
              () -> paymentCardService.updatePaymentCardStatusById(id, false));
      assertEquals(exception.getMessage(), String.format("%s not found with %s: '%s'",
              "Card", "id",id));
      verify(paymentCardRepository).findById(id);
      verify(userCacheService, never()).evictUserCacheWithCards(any());
    }

  }
}