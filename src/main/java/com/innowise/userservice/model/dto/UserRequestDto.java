package com.innowise.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for creating or updating a user")
public class UserRequestDto {

  @Schema(
          description = "User's first name",
          example = "John",
          requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotBlank(message = "Name is required")
  private String name;

  @Schema(
          description = "User's last name",
          example = "Doe",
          requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotBlank(message = "Surname is required")
  private String surname;

  @Schema(
          description = "User's email address",
          example = "john.doe@example.com",
          requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  @Schema(
          description = "User's date of birth",
          example = "1990-01-15",
          requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotNull(message = "Birth date is required")
  @Past(message = "Birth date must be in the past")
  private LocalDate birthDate;

  @Schema(
          description = "User's active status",
          example = "true",
          defaultValue = "true"
  )
  private boolean active= true;
}
