package com.library.books.model.pojo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", unique = true)
    private String nombre;

    @Column(name = "autor")
    private String autor;

    @Column(name = "ano")
    private Integer ano;

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "portada")
    private String portada;

    @Column(name = "sinopsis")
    private String sinopsis;

    @Column(name = "critica")
    private Double critica;

    @Column(name = "idioma")
    private String idioma;

    public void update(BookDto bookDto) {
        this.nombre = bookDto.getNombre();
        this.autor = bookDto.getAutor();
        this.ano = bookDto.getAno();
        this.isbn = bookDto.getIsbn();
        this.portada = bookDto.getPortada();
        this.sinopsis = bookDto.getSinopsis();
        this.critica = bookDto.getCritica();
        this.idioma = bookDto.getIdioma();
    }
}
