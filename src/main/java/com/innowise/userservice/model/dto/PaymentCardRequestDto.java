package com.innowise.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for creating or updating a payment card")
public class PaymentCardRequestDto {
  @Schema(
          description = "Card number (16 digits)",
          example = "4111111111111111",
          pattern = "\\d{16}",
          requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotBlank(message = "Card number is required")
  @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
  private String number;

  @Schema(
          description = "Card holder name",
          example = "JOHN DOE",
          minLength = 2,
          maxLength = 100,
          requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotBlank(message = "Card holder is required")
  @Size(min = 2, max = 100, message = "Holder name must be between 2 and 100 characters")
  private String holder;

  @Schema(
          description = "Card expiration date",
          example = "2026-12-31",
          requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NotNull(message = "Expiration date is required")
  @Future(message = "Expiration date must be in the future")
  private LocalDate expirationDate;

  @Schema(
          description = "Card active status",
          example = "true",
          defaultValue = "true"
  )
  private boolean active = true;

  @Schema(
          description = "ID of the user who owns the card",
          example = "1",
          requiredMode = Schema.RequiredMode.REQUIRED
  )
  private Long userId;
}
