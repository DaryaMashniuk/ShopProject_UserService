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

import java.util.List;
import java.util.Map;

@Tag(name = "User Management", description = "API for managing system users")
@RequestMapping(ApiConstants.USERS)
public interface UserControllerApi {

  @Operation(summary = "Create new user", description = "Creates a new user in the system")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "201",
                  description = "User successfully created",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = UserResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Invalid user data provided",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "409",
                  description = "User with this email already exists",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @PostMapping
  ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto userRequestDto);

  @Operation(summary = "Get all users", description = "Retrieves paginated list of all users")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Users retrieved successfully",
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
  ResponseEntity<PageResponseDto<UserResponseDto>> getUsers(@ParameterObject Pageable pageable);

  @Operation(summary = "Get user by ID", description = "Retrieves user information by user ID")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "User found and returned",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = UserResponseDto.class)
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
  @GetMapping(ApiConstants.BY_ID)
  ResponseEntity<UserResponseDto> getUserById(@PathVariable("id") Long id);

  @Operation(summary = "Update user", description = "Updates existing user information")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "User updated successfully",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = UserResponseDto.class)
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
                  description = "User not found",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @PutMapping(ApiConstants.BY_ID)
  ResponseEntity<UserResponseDto> updateUser(@PathVariable("id") Long id,
                                                    @RequestBody @Valid UserRequestDto userRequestDto);

  @Operation(summary = "Activate user", description = "Activates a deactivated user account")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "User activated successfully"
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
  @PatchMapping(ApiConstants.ACTIVATE)
  ResponseEntity<Void> updateUserActivate(@PathVariable("id") Long id);

  @Operation(summary = "Deactivate user", description = "Deactivates an active user account")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "User deactivated successfully"
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
  @PatchMapping(ApiConstants.DEACTIVATE)
  ResponseEntity<Void> updateUserDeactivate(@PathVariable("id") Long id);

  @Operation(summary = "Get user with cards", description = "Retrieves user information along with their payment cards")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "User with cards retrieved successfully",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = UserWithCardsDto.class)
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
  @GetMapping(ApiConstants.BY_ID_WITH_CARDS)
  ResponseEntity<UserWithCardsDto> getUserWithCards(@PathVariable("id") Long id);

  @Operation(summary = "Search users by criteria", description = "Searches users based on provided criteria with pagination")
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
  ResponseEntity<PageResponseDto<UserResponseDto>> getUsersByCriteria(
          @ParameterObject Pageable pageable,
          @RequestBody UserSearchCriteriaDto searchCriteria
  );

  @Operation(summary = "Delete user", description = "Permanently deletes a user from the system")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "204",
                  description = "User deleted successfully"
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
  @DeleteMapping(ApiConstants.BY_ID)
  ResponseEntity<Void> deleteUser(@PathVariable("id") Long id);

  @Operation(summary = "Get active users", description = "Retrieves all active users in the system")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Active users retrieved successfully",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = List.class)
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
  @GetMapping(ApiConstants.ACTIVE)
  ResponseEntity<List<UserResponseDto>> getActiveUsers();
}