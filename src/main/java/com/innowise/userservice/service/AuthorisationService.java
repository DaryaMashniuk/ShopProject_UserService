package com.innowise.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("authorisationService")
public class AuthorisationService {

  private final PaymentCardService paymentCardService;

  public boolean isSelf(Long userId, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    return authentication.getName().equals(String.valueOf(userId));
  }

  public boolean hasAdminRole(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }

  public boolean isSelfCard(Long cardId, Authentication authentication) {

    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    Long authUserId = Long.valueOf(authentication.getName());

    return paymentCardService.isCardOwnedByUser(cardId, authUserId);
  }
}

