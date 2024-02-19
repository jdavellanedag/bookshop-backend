package com.library.books.service;

import com.library.books.data.DataAccessRepository;
import com.library.books.model.db.Book;
import com.library.books.model.request.CreateBookRequest;
import com.library.books.model.response.BooksQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
public class BooksServiceImpl implements BooksService {

	private final DataAccessRepository repository;

	@Override
	public BooksQueryResponse getBooks(String search, String anoPublicacion, String idioma, Boolean aggregate) {
		//Ahora por defecto solo devolvera productos visibles
		return repository.findBooks(search, anoPublicacion, idioma, aggregate);
	}

	@Override
	public Book getBook(String bookId) {
		return repository.findById(bookId).orElse(null);
	}

	@Override
	public Boolean removeBook(String bookId) {

		Book book = repository.findById(bookId).orElse(null);
		if (book != null) {
			repository.delete(book);
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	@Override
	public Book createBook(CreateBookRequest request) {

		if (request != null && StringUtils.hasLength(request.getNombre().trim())
				&& StringUtils.hasLength(request.getAutor().trim())
				&& StringUtils.hasLength(request.getAnoPublicacion().trim())
				&& StringUtils.hasLength(request.getIsbn().trim())
				&& StringUtils.hasLength(request.getPortada().trim())
				&& StringUtils.hasLength(request.getSinopsis().trim())
				&& request.getCritica() != null
				&& StringUtils.hasLength(request.getIdioma().trim()) && request.getDisponible() != null) {

			Book book = Book.builder().nombre(request.getNombre()).autor(request.getAutor())
					.anoPublicacion(request.getAnoPublicacion()).isbn(request.getIsbn())
					.portada(request.getPortada()).sinopsis(request.getSinopsis())
					.critica(request.getCritica()).disponible(request.getDisponible()).build();

			return repository.save(book);
		} else {
			return null;
		}
	}

}
