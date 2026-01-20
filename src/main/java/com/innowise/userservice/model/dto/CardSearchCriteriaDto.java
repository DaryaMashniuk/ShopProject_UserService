package com.innowise.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Search criteria for payment cards")
public class CardSearchCriteriaDto {

  @Schema(example = "1234567812345678")
  private String number;

  @Schema(example = "John Doe")
  private String holder;

  @Schema(example = "true")
  private Boolean active;
}

