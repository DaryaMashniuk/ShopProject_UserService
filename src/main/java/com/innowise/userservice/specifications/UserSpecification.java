package com.innowise.userservice.specifications;

import com.innowise.userservice.model.User;
import com.innowise.userservice.model.dto.UserSearchCriteriaDto;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class UserSpecification {

  public static Specification<User> build (UserSearchCriteriaDto userSearchCriteriaDto) {
    return Specification.where(containsFirstNameCaseInsensitive(userSearchCriteriaDto.getName()))
            .and(containsSurnameCaseInsensitive(userSearchCriteriaDto.getSurname()))
            .and(containsEmailCaseInsensitive(userSearchCriteriaDto.getEmail()))
            .and(hasActiveStatus(userSearchCriteriaDto.getActive()));
  }

  private static Specification<User> containsFirstNameCaseInsensitive(String firstName) {
    return SpecificationUtils.likeIgnoreCase("name",firstName );
  }

  private static Specification<User> containsSurnameCaseInsensitive(String surname) {
    return SpecificationUtils.likeIgnoreCase("surname",surname );
  }

  private static Specification<User> containsEmailCaseInsensitive(String email) {
    return SpecificationUtils.likeIgnoreCase("email",email );
  }

  private static Specification<User> hasActiveStatus(Boolean active) {
    return SpecificationUtils.equalsBoolean("active",active);
  }

}
