package com.library.books.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.library.books.data.BookRepository;
import com.library.books.model.pojo.Book;
import com.library.books.model.pojo.BookDto;
import com.library.books.model.request.CreateBookRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Slf4j
public class BooksServiceImpl implements BooksService {

    @Autowired
    private BookRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Book> getBooks(String nombre, String autor, Integer ano, String isbn, String idioma) {

        if (StringUtils.hasLength(nombre) || StringUtils.hasLength(autor) || ano != null
                || StringUtils.hasLength(isbn) || StringUtils.hasLength(idioma)) {
            return repository.search(nombre, autor, ano, isbn, idioma);
        }

        List<Book> books = repository.getBooks();
        return books.isEmpty() ? null : books;
    }

    @Override
    public Book getBook(String bookId) {
        return repository.getById(Long.valueOf(bookId));
    }

    @Override
    public Boolean removeBook(String bookId) {

        Book book = repository.getById(Long.valueOf(bookId));

        if (book != null) {
            repository.delete(book);
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public Book createBook(CreateBookRequest request) {

        //Otra opcion: Jakarta Validation: https://www.baeldung.com/java-validation
        if (request != null && StringUtils.hasLength(request.getNombre().trim())
                && StringUtils.hasLength(request.getAutor().trim())
                && request.getAno() != null && StringUtils.hasLength(request.getIsbn().trim()) &&
                StringUtils.hasLength(request.getPortada().trim()) && StringUtils.hasLength(request.getSinopsis().trim())
                && request.getCritica() != null && StringUtils.hasLength(request.getIdioma().trim())) {

            Book book = Book.builder().nombre(request.getNombre()).autor(request.getAutor())
                    .ano(request.getAno()).isbn(request.getIsbn())
                    .portada(request.getPortada()).sinopsis(request.getSinopsis())
                    .critica(request.getCritica()).idioma(request.getIdioma()).build();

            return repository.save(book);
        } else {
            return null;
        }
    }

    @Override
    public Book updateBook(String bookId, String request) {

        //PATCH se implementa en este caso mediante Merge Patch: https://datatracker.ietf.org/doc/html/rfc7386
        Book book = repository.getById(Long.valueOf(bookId));
        if (book != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(book)));
                Book patched = objectMapper.treeToValue(target, Book.class);
                repository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating product {}", bookId, e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Book updateBook(String bookId, BookDto updateRequest) {
        Book book = repository.getById(Long.valueOf(bookId));
        if (book != null) {
            book.update(updateRequest);
            repository.save(book);
            return book;
        } else {
            return null;
        }
    }
}
