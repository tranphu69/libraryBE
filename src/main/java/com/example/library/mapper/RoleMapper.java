package com.example.library.mapper;

import com.example.library.domain.Role;
import com.example.library.dto.response.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = PermissionMapper.class
)
public interface RoleMapper {

    @Mapping(source = "publicId", target = "id")
    @Mapping(source = "permissions", target = "listPermission")
    RoleResponse toRoleResponse(Role role);
}
