package com.innowise.userservice.specifications;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class SpecificationUtils {

  public static <T> Specification<T> likeIgnoreCase(String field, String value) {
    return (root, query, cb) ->
            value == null || value.isBlank()
            ? null
            : cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%");
  }

  public static <T> Specification<T> equalsBoolean(String field, Boolean value) {
    return (root, query, cb) ->
            value == null ? null : cb.equal(root.get(field), value);
  }

}
