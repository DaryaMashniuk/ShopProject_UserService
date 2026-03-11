package com.innowise.userservice.mapper;

import com.innowise.userservice.model.User;
import com.innowise.userservice.model.dto.UserRequestDto;
import com.innowise.userservice.model.dto.UserResponseDto;
import com.innowise.userservice.model.dto.UserWithCardsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;


@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = PaymentCardMapper.class
)
public interface UserMapper {
  @Mapping(target = "paymentCards", ignore = true)
  User toEntity(UserRequestDto dto);


  UserResponseDto toResponseDto(User user);

  @Mapping(target = "paymentCards", source = "paymentCards")
  UserWithCardsDto toWithCardsDto(User user);

  List<UserResponseDto> toResponseDtoList(List<User> users);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "paymentCards", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromDto(UserRequestDto dto, @MappingTarget User user);
}
