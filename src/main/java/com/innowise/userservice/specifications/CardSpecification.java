package com.innowise.userservice.specifications;

import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.dto.CardSearchCriteriaDto;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class CardSpecification {

  public static Specification<PaymentCard> build(CardSearchCriteriaDto searchCriteria) {
    return Specification.where(hasNumber(searchCriteria.getNumber()))
            .and(containsHolderCaseInsensitive(searchCriteria.getHolder()))
            .and(isActive(searchCriteria.getActive()));
  }

  private static Specification<PaymentCard> hasNumber(String number) {
    return (root, query, criteriaBuilder) -> (number == null || number.isBlank())
            ? null : criteriaBuilder.like(root.get("number"), "%" +number+ "%");
  }

  private static Specification<PaymentCard> containsHolderCaseInsensitive(String holder) {
    return (root, query, criteriaBuilder) ->(holder == null || holder.isBlank())
            ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("holder")), "%" + holder.toLowerCase() + "%");
  }

  private static Specification<PaymentCard> isActive(Boolean active) {
    return (root, query, cb) -> (active == null)
            ? null : cb.equal(root.get("active"), active);
  }
}
