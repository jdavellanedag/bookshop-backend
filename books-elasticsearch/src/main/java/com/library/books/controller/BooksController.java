package com.library.books.controller;

import java.util.Map;

import com.library.books.model.db.Book;
import com.library.books.model.request.CreateBookRequest;
import com.library.books.model.response.BooksQueryResponse;
import com.library.books.service.BooksService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BooksController {

	private final BooksService service;

	@GetMapping("/books")
	public ResponseEntity<BooksQueryResponse> getBooks(
			@RequestHeader Map<String, String> headers,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String anoPublicacion,
			@RequestParam(required = false) String idioma,
			@RequestParam(required = false, defaultValue = "false") Boolean aggregate) {

		log.info("headers: {}", headers);
		BooksQueryResponse books = service.getBooks(search, anoPublicacion, idioma, aggregate);
		return ResponseEntity.ok(books);
	}

	@GetMapping("/books/{bookId}")
	public ResponseEntity<Book> getBook(@PathVariable String bookId) {

		log.info("Request received for product {}", bookId);
		Book book = service.getBook(bookId);

		if (book != null) {
			return ResponseEntity.ok(book);
		} else {
			return ResponseEntity.notFound().build();
		}

	}

	@DeleteMapping("/books/{bookId}")
	public ResponseEntity<Void> deleteBook(@PathVariable String bookId) {

		Boolean removed = service.removeBook(bookId);

		if (Boolean.TRUE.equals(removed)) {
			return ResponseEntity.ok().build();
		} else {
			return ResponseEntity.notFound().build();
		}

	}

	@PostMapping("/books")
	public ResponseEntity<Book> getBook(@RequestBody CreateBookRequest request) {

		Book createdBook = service.createBook(request);

		if (createdBook != null) {
			return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
		} else {
			return ResponseEntity.badRequest().build();
		}

	}

	@PatchMapping("/books/{bookId}")
	public ResponseEntity<Book> updateBook(@PathVariable String bookId, @RequestBody String patchBody) {

		Book updatedBook = service.updateBook(bookId, patchBody);

		if (updatedBook != null) {
			return ResponseEntity.ok(updatedBook);
		} else {
			return ResponseEntity.badRequest().build();
		}

	}

}
