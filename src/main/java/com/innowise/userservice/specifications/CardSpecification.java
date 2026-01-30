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
    return SpecificationUtils.likeIgnoreCase("number", number);
  }

  private static Specification<PaymentCard> containsHolderCaseInsensitive(String holder) {
    return SpecificationUtils.likeIgnoreCase("holder", holder);
  }

  private static Specification<PaymentCard> isActive(Boolean active) {
    return SpecificationUtils.equalsBoolean("active", active);
  }
}
