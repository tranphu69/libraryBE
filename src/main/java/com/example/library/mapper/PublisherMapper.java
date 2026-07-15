package com.example.library.mapper;

import com.example.library.domain.Publisher;
import com.example.library.dto.response.PublisherResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PublisherMapper {
    PublisherResponse toPublisherResponse(Publisher publisher);
}
