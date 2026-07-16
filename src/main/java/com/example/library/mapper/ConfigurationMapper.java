package com.example.library.mapper;

import com.example.library.domain.Configuration;
import com.example.library.dto.response.ConfigurationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfigurationMapper {
    ConfigurationResponse toConfigurationResponse(Configuration configuration);
}
