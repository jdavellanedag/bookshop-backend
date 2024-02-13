package com.library.books.service;


import com.library.books.model.request.CreateBookRequest;
import com.library.books.model.response.BooksQueryResponse;
import com.library.books.model.db.Book;

public interface BooksService {

	BooksQueryResponse getBooks(String nombre, String autor, String anoPublicacion, String isbn, String sinopsis, String idioma, Boolean aggregate);
	
	Book getBook(String bookId);
	
	Boolean removeBook(String bookId);
	
	Book createBook(CreateBookRequest request);

}
