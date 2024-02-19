package com.library.books.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.library.books.data.DataAccessRepository;
import com.library.books.model.db.Book;
import com.library.books.model.request.CreateBookRequest;
import com.library.books.model.response.BooksQueryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
public class BooksServiceImpl implements BooksService {

	private final DataAccessRepository repository;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public BooksQueryResponse getBooks(String search, String anoPublicacion, String idioma, Boolean aggregate) {
		//Ahora por defecto solo devolverá productos visibles
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

	@Override
	public Book updateBook(String bookId, String request) {

		//PATCH se implementa en este caso mediante Merge Patch: https://datatracker.ietf.org/doc/html/rfc7386
		Book book = repository.findById(bookId).orElse(null);
		if (book != null) {
			try {
				JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
				JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(book)));
				Book patched = objectMapper.treeToValue(target, Book.class);
				repository.save(patched);
				return patched;
			} catch (JsonProcessingException | JsonPatchException e) {
				return null;
			}
		} else {
			return null;
		}
	}

}
