package com.innowise.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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
  @Size(min = 2, max = 50, message = "Name search must be between 2 and 50 characters")
  private String name;

  @Schema(description = "User surname (partial match, case-insensitive)",
          example = "Smith")
  @Size(min = 2, max = 50, message = "Surname search must be between 2 and 50 characters")
  private String surname;

  @Schema(description = "User email (partial match, case-insensitive)",
          example = "gmail.com")
  @Email(message = "Search email must be a valid format")
  private String email;

  @Schema(description = "User active status",
          example = "true")
  private Boolean active;
}
