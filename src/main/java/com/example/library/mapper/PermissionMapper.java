package com.example.library.mapper;

import com.example.library.domain.Permission;
import com.example.library.dto.response.PermissionResponse;
import com.example.library.dto.response.SimpleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    @Mapping(source = "publicId", target = "id")
    PermissionResponse toPermissionResponse(Permission permission);

    @Mapping(source = "publicId", target = "id")
    SimpleResponse toSimpleResponse(Permission permission);
}
