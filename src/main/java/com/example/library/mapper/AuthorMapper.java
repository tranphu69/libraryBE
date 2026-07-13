package com.example.library.mapper;

import com.example.library.domain.Author;
import com.example.library.dto.response.AuthorResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    AuthorResponse toAuthorResponse(Author author);
}
