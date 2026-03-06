package com.innowise.userservice.controller.api;

import com.innowise.userservice.model.dto.ChangeStatusRequestDto;
import com.innowise.userservice.model.dto.ErrorResponse;
import com.innowise.userservice.model.dto.PageResponseDto;
import com.innowise.userservice.model.dto.UserRequestDto;
import com.innowise.userservice.model.dto.UserResponseDto;
import com.innowise.userservice.model.dto.UserWithCardsDto;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "User Management", description = "API for managing system users")
@RequestMapping("/api/v1/users")
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
                  responseCode = "500",
                  description = "Internal server error",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @GetMapping
  ResponseEntity<PageResponseDto<UserResponseDto>> getUsers(
          @RequestParam(required = false) String name,
          @RequestParam(required = false) String surname,
          @RequestParam(required = false) String email,
          @RequestParam(required = false) Boolean active,
          @ParameterObject Pageable pageable);

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
  @GetMapping("/{id}")
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
  @PutMapping("/{id}")
  ResponseEntity<UserResponseDto> updateUser(@PathVariable("id") Long id,
                                                    @RequestBody @Valid UserRequestDto userRequestDto);

  @Operation(summary = "Deactivate/Activate user", description = "Changes status of the user account")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "User status changed successfully"
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
  @PatchMapping("/{id}")
  ResponseEntity<Void> updateUserStatus(
          @PathVariable("id") Long id,
          @RequestBody @Valid ChangeStatusRequestDto statusDto);

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
  @GetMapping("/{id}/cards")
  ResponseEntity<UserWithCardsDto> getUserWithCards(@PathVariable("id") Long id);

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
  @DeleteMapping("/{id}")
  ResponseEntity<Void> deleteUser(@PathVariable("id") Long id);

  @Operation(
          summary = "Get users by batch IDs",
          description = "Retrieves multiple users by their IDs in a single request. Useful for batch operations and reducing network calls when multiple user records are needed."
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Users successfully retrieved. Returns an array of user objects. The list may be empty if no users found for the provided IDs.",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = UserResponseDto.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Invalid request parameters. Possible causes: empty IDs list, malformed ID values, or invalid ID format.",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "403",
                  description = "Access denied. Only users with ADMIN role can access this endpoint.",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          ),
          @ApiResponse(
                  responseCode = "500",
                  description = "Internal server error occurred while processing the request",
                  content = @Content(
                          mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)
                  )
          )
  })
  @GetMapping("/batch")
  ResponseEntity<List<UserResponseDto>> getUsersByIds(@RequestParam List<Long> ids);
}