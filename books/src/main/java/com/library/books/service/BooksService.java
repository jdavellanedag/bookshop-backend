package com.library.books.service;

import com.library.books.model.pojo.Book;
import com.library.books.model.pojo.BookDto;
import com.library.books.model.request.CreateBookRequest;

import java.util.List;

public interface BooksService {

    List<Book> getBooks(String nombre, String autor, Integer ano, String isbn, String idioma);

    Book getBook(String bookId);

    Boolean removeBook(String bookId);

    Book createBook(CreateBookRequest request);

    Book updateBook(String bookId, String updateRequest);

    Book updateBook(String bookId, BookDto updateRequest);
}
