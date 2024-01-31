package com.library.books.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateBookRequest {
    private String nombre;

    private String autor;

    private Integer ano;

    private String isbn;

    private String portada;

    private String sinopsis;

    private Double critica;

    private String idioma;
}
