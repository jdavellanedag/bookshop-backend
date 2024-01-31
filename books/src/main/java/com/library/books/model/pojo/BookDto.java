package com.library.books.model.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BookDto {
    private String nombre;

    private String autor;

    private Integer ano;

    private String isbn;

    private String portada;

    private String sinopsis;

    private Double critica;

    private String idioma;
}
