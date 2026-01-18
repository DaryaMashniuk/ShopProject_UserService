package com.innowise.userservice.mapper;

import com.innowise.userservice.model.PaymentCard;
import com.innowise.userservice.model.dto.PaymentCardRequestDto;
import com.innowise.userservice.model.dto.PaymentCardResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = UserMapper.class
)
public interface PaymentCardMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  PaymentCard toEntity(PaymentCardRequestDto dto);

  @Mapping(target = "userId", source = "user.id")
  PaymentCardResponseDto toResponseDto(PaymentCard paymentCard);

  List<PaymentCardResponseDto> toResponseDtoList(List<PaymentCard> paymentCards);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntityFromDto(PaymentCardRequestDto dto, @MappingTarget PaymentCard paymentCard);
}
