package com.library.orders.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Book {
	private Long id;

	private String nombre;

	private String autor;

	private Integer ano;

	private String isbn;

	private String portada;

	private String sinopsis;

	private Double critica;

	private String idioma;
}
