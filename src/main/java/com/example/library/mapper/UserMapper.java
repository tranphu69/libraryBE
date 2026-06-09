package com.example.library.mapper;

import com.example.library.domain.User;
import com.example.library.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = RoleMapper.class
)
public interface UserMapper {
    @Mapping(source = "roles", target ="listRole")
    UserResponse toUserResponse(User user);
}
