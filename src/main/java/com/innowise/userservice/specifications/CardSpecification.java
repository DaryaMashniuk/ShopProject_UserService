package com.innowise.userservice.specifications;

import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.User;
import org.springframework.data.jpa.domain.Specification;

public class CardSpecification {
  public static Specification<PaymentCard> hasNumber(String number) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("number"), number);
  }

  public static Specification<PaymentCard> containsHolderCaseInsensitive(String holder) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("holder")), "%" + holder.toLowerCase() + "%");
  }
}
