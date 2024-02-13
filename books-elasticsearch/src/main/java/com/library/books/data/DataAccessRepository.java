package com.library.books.data;

import java.util.*;

import com.library.books.model.response.AggregationDetails;

import com.library.books.model.db.Book;
import com.library.books.model.response.BooksQueryResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DataAccessRepository {

    @Value("${server.fullAddress}")
    private String serverFullAddress;

    // Esta clase (y bean) es la unica que usan directamente los servicios para
    // acceder a los datos.
    private final BookRepository bookRepository;
    private final ElasticsearchOperations elasticClient;

    private final String[] nombreSearchFields = {"Nombre", "Nombre._2gram", "Nombre._3gram"};
    private final String[] autorSearchFields = {"Autor", "Autor._2gram", "Autor._3gram"};

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public Boolean delete(Book book) {
        bookRepository.delete(book);
        return Boolean.TRUE;
    }

	public Optional<Book> findById(String id) {
		return bookRepository.findById(id);
	}

    @SneakyThrows
    public BooksQueryResponse findBooks(String nombre, String autor, String anoPublicacion, String isbn, String sinopsis, String idioma, Boolean aggregate) {

        BoolQueryBuilder querySpec = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(nombre)) {
            querySpec.must(QueryBuilders.multiMatchQuery(nombre, nombreSearchFields).type(Type.BOOL_PREFIX));
        }

        if (!StringUtils.isEmpty(autor)) {
            querySpec.must(QueryBuilders.multiMatchQuery(autor, autorSearchFields).type(Type.BOOL_PREFIX));
        }

        if (!StringUtils.isEmpty(anoPublicacion)) {
            querySpec.must(QueryBuilders.termQuery("AnoPublicacion", anoPublicacion));
        }

        if (!StringUtils.isEmpty(isbn)) {
            querySpec.must(QueryBuilders.termQuery("ISBN", isbn));
        }

        if (!StringUtils.isEmpty(sinopsis)) {
            querySpec.must(QueryBuilders.matchQuery("Sinopsis", sinopsis));
        }

        if (!StringUtils.isEmpty(idioma)) {
            querySpec.must(QueryBuilders.termQuery("Idioma", idioma));
        }

        //Si no he recibido ningun parametro, busco todos los elementos.
        if (!querySpec.hasClauses()) {
            querySpec.must(QueryBuilders.matchAllQuery());
        }

        //Filtro implicito
        //No le pido al usuario que lo introduzca pero lo aplicamos proactivamente en todas las peticiones
        //En este caso, que los productos sean visibles (estado correcto de la entidad)
        querySpec.must(QueryBuilders.termQuery("Disponible", true));

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(querySpec);

        if (Boolean.TRUE.equals(aggregate)) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("Idioma Aggregation").field("Idioma").size(1000));
            nativeSearchQueryBuilder.withMaxResults(0);
        }

        //Opcionalmente, podemos paginar los resultados
        //nativeSearchQueryBuilder.withPageable(PageRequest.of(0, 10));

        Query query = nativeSearchQueryBuilder.build();
        SearchHits<Book> result = elasticClient.search(query, Book.class);

        List<AggregationDetails> responseAggs = new LinkedList<>();

        if (result.hasAggregations()) {
            Map<String, Aggregation> aggs = result.getAggregations().asMap();
            ParsedStringTerms idiomaAgg = (ParsedStringTerms) aggs.get("Idioma Aggregation");

            //Componemos una URI basada en serverFullAddress y query params para cada argumento, siempre que no viniesen vacios
            String queryParams = getQueryParams(nombre, autor, anoPublicacion, isbn, sinopsis, idioma);
            idiomaAgg.getBuckets()
                    .forEach(
                            bucket -> responseAggs.add(
                                    new AggregationDetails(
                                            bucket.getKey().toString(),
                                            (int) bucket.getDocCount(),
                                            serverFullAddress + "/books?idioma=" + bucket.getKey() + queryParams)));
        }
        return new BooksQueryResponse(result.getSearchHits().stream().map(SearchHit::getContent).toList(), responseAggs);
    }

    /**
     * Componemos una URI basada en serverFullAddress y query params para cada argumento, siempre que no viniesen vacios
     *
     * @param nombre        - nombre del libro
     * @param autor - autor del libro
     * @param anoPublicacion     - año de publicación del libro
     * @param isbn     - isbn del libro
     * @param sinopsis     - sinopsis del libro
     * @param idioma     - idioma del libro
     * @return
     */
    private String getQueryParams(String nombre, String autor, String anoPublicacion, String isbn, String sinopsis, String idioma) {
        String queryParams = (StringUtils.isEmpty(nombre) ? "" : "&nombre=" + nombre)
                + (StringUtils.isEmpty(autor) ? "" : "&autor=" + autor)
                + (StringUtils.isEmpty(anoPublicacion) ? "" : "&anoPublicacion=" + anoPublicacion)
                + (StringUtils.isEmpty(isbn) ? "" : "&isbn=" + isbn)
                + (StringUtils.isEmpty(sinopsis) ? "" : "&sinopsis=" + sinopsis)
                + (StringUtils.isEmpty(idioma) ? "" : "&idioma=" + idioma);
        // Eliminamos el ultimo & si existe
        return queryParams.endsWith("&") ? queryParams.substring(0, queryParams.length() - 1) : queryParams;
    }
}
