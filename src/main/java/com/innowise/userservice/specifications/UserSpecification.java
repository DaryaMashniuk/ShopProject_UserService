package com.innowise.userservice.specifications;

import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import com.innowise.userservice.model.dto.UserSearchCriteriaDto;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

  public static Specification<User> build (UserSearchCriteriaDto userSearchCriteriaDto) {
    return Specification.where(containsFirstNameCaseInsensitive(userSearchCriteriaDto.getName()))
            .and(containsSurnameCaseInsensitive(userSearchCriteriaDto.getSurname()))
            .and(containsEmailCaseInsensitive(userSearchCriteriaDto.getEmail()))
            .and(hasActiveStatus(userSearchCriteriaDto.getActive()));
  }

  private static Specification<User> containsFirstNameCaseInsensitive(String firstName) {
    return (root, query, criteriaBuilder) -> (firstName == null || firstName.isBlank())
            ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + firstName.toLowerCase() + "%");
  }

  private static Specification<User> containsSurnameCaseInsensitive(String surname) {
    return (root, query, criteriaBuilder) -> (surname == null || surname.isBlank())
            ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")), "%" + surname.toLowerCase() + "%");
  }

  private static Specification<User> containsEmailCaseInsensitive(String email) {
    return (root, query, criteriaBuilder) ->(email == null || email.isBlank())
            ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
  }

  private static Specification<User> hasActiveStatus(Boolean active) {
    return (root, query, cb) ->(active == null)
            ? null : cb.equal(root.get("active"), active);
  }

}
