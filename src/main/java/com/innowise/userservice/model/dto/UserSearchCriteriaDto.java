package com.innowise.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Search criteria for filtering users")
public class UserSearchCriteriaDto implements Serializable {

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
