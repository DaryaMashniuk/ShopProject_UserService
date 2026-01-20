package com.innowise.userservice.specifications;

import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

  public static Specification<User> containsFirstNameCaseInsensitive(String firstName) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + firstName.toLowerCase() + "%");
  }

  public static Specification<User> containsSurnameCaseInsensitive(String surname) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")), "%" + surname.toLowerCase() + "%");
  }

  public static Specification<User> containsEmailCaseInsensitive(String email) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
  }

  public static Specification<User> hasActiveStatus(Boolean active) {
    return (root, query, cb) -> cb.equal(root.get("active"), active);
  }

}
