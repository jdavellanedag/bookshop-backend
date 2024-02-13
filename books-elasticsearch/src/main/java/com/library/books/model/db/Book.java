package com.library.books.model.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document(indexName = "books", createIndex = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructo
@Builder
@ToString
public class Book {
	
	@Id
	private String id;
	
	@Field(type = FieldType.Search_As_You_Type, name = "Nombre")
	private String nombre;
	
	@Field(type = FieldType.Search_As_You_Type, name = "Autor")
	private String autor;
	
	@Field(type = FieldType.Date, name = "AnoPublicacion")
	private Date anoPublicacion;

	@Field(type = FieldType.Keyword, name = "ISBN")
	private String isbn;

	@Field(type = FieldType.Text, name = "Portada")
	private String portada;

	@Field(type = FieldType.Text, name = "Sinopsis")
	private String sinopsis;

	@Field(type = FieldType.Double, name = "Critica")
	private Double critica;

	@Field(type = FieldType.Keyword, name = "Idioma")
	private String idioma;
	
	@Field(type = FieldType.Boolean, name = "Disponible")
	private Boolean disponible;

}
