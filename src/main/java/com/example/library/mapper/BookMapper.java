package com.example.library.mapper;

import com.example.library.domain.Book;
import com.example.library.dto.response.BookResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookResponse toBookResponse(Book book);
}
