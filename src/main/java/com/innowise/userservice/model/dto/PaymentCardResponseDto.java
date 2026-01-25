package com.innowise.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for displaying payment card information")
public class PaymentCardResponseDto implements Serializable {
  @Schema(description = "Unique card identifier", example = "1")
  private Long id;
  @Schema(description = "Card number (masked for security)", example = "411111******1111")
  private String number;
  @Schema(description = "Card holder name", example = "JOHN DOE")
  private String holder;
  @Schema(description = "Card expiration date", example = "2026-12-31")
  private LocalDate expirationDate;
  @Schema(description = "Active status", example = "true")
  private boolean active;
  @Schema(description = "ID of the user who owns the card", example = "1")
  private Long userId;
  @Schema(description = "Record creation timestamp", example = "2024-01-15T10:30:00")
  private LocalDateTime createdAt;
  @Schema(description = "Record last update timestamp", example = "2024-01-15T10:30:00")
  private LocalDateTime updatedAt;
}
