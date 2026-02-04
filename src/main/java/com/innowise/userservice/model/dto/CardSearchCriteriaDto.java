package com.innowise.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Search criteria for payment cards")
@AllArgsConstructor
@NoArgsConstructor
public class CardSearchCriteriaDto {

  @Schema(example = "1234567812345678")
  @Pattern(regexp = "\\d{1,16}", message = "Search number must be numeric and up to 16 digits")
  private String number;

  @Schema(example = "John Doe")
  @Size(max = 100, message = "Holder name search is too long")
  private String holder;

  @Schema(example = "true")
  private Boolean active;
}

