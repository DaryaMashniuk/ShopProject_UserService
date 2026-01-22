package com.innowise.userservice.controller.api;

import com.innowise.userservice.constants.ApiConstants;
import com.innowise.userservice.model.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Tag(name = "Payment Card Management", description = "API for managing user payment cards")
@RequestMapping(ApiConstants.CARDS)
public interface CardControllerApi {

  @Operation(summary = "Create new payment card", description = "Links a new payment card to a user")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "201",
                  description = "Payment card successfully created and linked",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = PaymentCardResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Invalid card data or maximum card limit reached",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "User not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @PostMapping
  ResponseEntity<PaymentCardResponseDto> createCard(@RequestBody @Valid PaymentCardRequestDto paymentCardRequestDto);

  @Operation(summary = "Get all payment cards", description = "Retrieves paginated list of all payment cards")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Payment cards retrieved successfully",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = PageResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "500",
                  description = "Internal server error",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @GetMapping
  ResponseEntity<PageResponseDto<PaymentCardResponseDto>> getAllPaymentCards(@ParameterObject Pageable pageable);

  @Operation(summary = "Get card by ID", description = "Retrieves payment card information by card ID")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Payment card found and returned",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = PaymentCardResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "Payment card not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @GetMapping(ApiConstants.BY_ID)
  ResponseEntity<PaymentCardResponseDto> getCardById(@PathVariable Long id);

  @Operation(summary = "Update payment card", description = "Updates existing payment card information")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Payment card updated successfully",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = PaymentCardResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Invalid update data provided",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "Payment card not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @PutMapping(ApiConstants.BY_ID)
  ResponseEntity<PaymentCardResponseDto> updateCard(@PathVariable Long id, @RequestBody @Valid PaymentCardRequestDto paymentCardRequestDto);

  @Operation(summary = "Activate payment card", description = "Activates a deactivated payment card")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Payment card activated successfully"
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "Payment card not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @PatchMapping(ApiConstants.ACTIVATE)
  ResponseEntity<Void> updateCardActivate(@PathVariable("id") Long id);

  @Operation(summary = "Deactivate payment card", description = "Deactivates an active payment card")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Payment card deactivated successfully"
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "Payment card not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @PatchMapping(ApiConstants.DEACTIVATE)
  ResponseEntity<Void> updateCardDeactivate(@PathVariable("id") Long id);

  @Operation(summary = "Search cards by criteria", description = "Searches payment cards based on provided criteria with pagination")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Search completed successfully",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = PageResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Invalid search criteria",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @PostMapping(ApiConstants.SEARCH)
  ResponseEntity<PageResponseDto<PaymentCardResponseDto>> getPaymentCardsByCriteria(
          @ParameterObject Pageable pageable,
          @RequestBody @Valid CardSearchCriteriaDto searchCriteria
  );

  @Operation(summary = "Delete payment card", description = "Permanently deletes a payment card from the system")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "204",
                  description = "Payment card deleted successfully"
          ),
          @ApiResponse(
                  responseCode = "404",
                  description = "Payment card not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @DeleteMapping(ApiConstants.BY_ID)
  ResponseEntity<Void> deleteCardById(@PathVariable Long id);
}