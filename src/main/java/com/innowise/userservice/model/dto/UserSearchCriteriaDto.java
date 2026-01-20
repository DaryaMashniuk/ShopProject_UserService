package com.innowise.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Search criteria for filtering users")
public class UserSearchCriteriaDto {

  @Schema(description = "User first name (partial match, case-insensitive)",
          example = "Ann")
  private String name;

  @Schema(description = "User surname (partial match, case-insensitive)",
          example = "Smith")
  private String surname;

  @Schema(description = "User email (partial match, case-insensitive)",
          example = "gmail.com")
  private String email;

  @Schema(description = "User active status",
          example = "true")
  private Boolean active;
}
