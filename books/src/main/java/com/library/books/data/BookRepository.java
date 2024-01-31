package com.library.books.data;

import com.library.books.data.utils.SearchCriteria;
import com.library.books.data.utils.SearchOperation;
import com.library.books.data.utils.SearchStatement;
import com.library.books.model.pojo.Book;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepository {

    private final BookJpaRepository repository;

    public List<Book> getBooks() {
        return repository.findAll();
    }

    public Book getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Book save(Book book) {
        return repository.save(book);
    }

    public void delete(Book book) {
        repository.delete(book);
    }

    public List<Book> search(String nombre, String autor, Integer ano, String isbn, String idioma) {
        SearchCriteria<Book> spec = new SearchCriteria<>();
        if (StringUtils.isNotBlank(nombre)) {
            spec.add(new SearchStatement("nombre", nombre, SearchOperation.MATCH));
        }

        if (StringUtils.isNotBlank(autor)) {
            spec.add(new SearchStatement("autor", autor, SearchOperation.MATCH));
        }

        if (ano != null) {
            spec.add(new SearchStatement("ano", ano, SearchOperation.EQUAL));
        }

        if (StringUtils.isNotBlank(isbn)) {
            spec.add(new SearchStatement("isbn", isbn, SearchOperation.EQUAL));
        }

        if (StringUtils.isNotBlank(idioma)) {
            spec.add(new SearchStatement("idioma", idioma, SearchOperation.EQUAL));
        }
        return repository.findAll(spec);
    }

}
