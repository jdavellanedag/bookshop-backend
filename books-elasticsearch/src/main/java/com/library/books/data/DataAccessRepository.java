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

    private final String[] searchSearchFields = {"Nombre", "Nombre._2gram", "Nombre._3gram", "Autor", "Autor._2gram", "Autor._3gram", "Sinopsis", "ISBN"};

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
    public BooksQueryResponse findBooks(String search, String anoPublicacion, String idioma, Boolean aggregate) {

        BoolQueryBuilder querySpec = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(search)) {
            querySpec.must(QueryBuilders.multiMatchQuery(search, searchSearchFields).type(Type.BOOL_PREFIX));
        }

        if (!StringUtils.isEmpty(anoPublicacion)) {
            querySpec.must(QueryBuilders.termQuery("AnoPublicacion", anoPublicacion));
        }

        if (!StringUtils.isEmpty(idioma)) {
            querySpec.must(QueryBuilders.termQuery("Idioma", idioma));
        }

        //Si no he recibido ningún parámetro, busco todos los elementos.
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
            String queryParams = getQueryParams(search, anoPublicacion, idioma);
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
     * @param search        - nombre, autor, isbn o sinopsis del libro
     * @param anoPublicacion     - año de publicación del libro
     * @param idioma     - idioma del libro
     * @return
     */
    private String getQueryParams(String search, String anoPublicacion, String idioma) {
        String queryParams = (StringUtils.isEmpty(search) ? "" : "&search=" + search)
                + (StringUtils.isEmpty(anoPublicacion) ? "" : "&anoPublicacion=" + anoPublicacion)
                + (StringUtils.isEmpty(idioma) ? "" : "&idioma=" + idioma);
        // Eliminamos el último & si existe
        return queryParams.endsWith("&") ? queryParams.substring(0, queryParams.length() - 1) : queryParams;
    }
}
