package com.library.books.data;

import java.util.List;
import java.util.Optional;

import com.library.books.model.db.Book;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface BookRepository extends ElasticsearchRepository<Book, String> {

	Optional<Book> findById(String id);
	
	Book save(Book book);
	
	void delete(Book book);
	
	List<Book> findAll();
}
