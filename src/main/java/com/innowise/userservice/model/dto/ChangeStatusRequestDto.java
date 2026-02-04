package com.innowise.userservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO for activating/deactivating")
public class ChangeStatusRequestDto {
  @Schema(
          description = "Status of the object",
          example = "true"
  )
  @NotNull
  private Boolean active;
}
