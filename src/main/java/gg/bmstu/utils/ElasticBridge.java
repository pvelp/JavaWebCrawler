package gg.bmstu.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import gg.bmstu.entity.NewsEntity;
import gg.bmstu.services.PageReciever;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElasticBridge {
    private final ObjectMapper mapper;
    private final RestClient restClient;
    private final ElasticsearchTransport elasticsearchTransport;
    private final ElasticsearchClient elasticsearchClient;
    private final String INDEX_NAME;
    private final static Logger logger = LoggerFactory.getLogger(PageReciever.class);

    public ElasticBridge(String elUrl, String indexName) {
        mapper = JsonMapper.builder().build();
        restClient = RestClient
                .builder(HttpHost.create(elUrl)).build();
        elasticsearchTransport = new RestClientTransport(restClient,
                new JacksonJsonpMapper(mapper));
        elasticsearchClient = new ElasticsearchClient(elasticsearchTransport);
        INDEX_NAME = indexName;
        logger.info("Elastic connection success!");
    }

    public void createIndex() throws IOException {
        BooleanResponse index_exists = elasticsearchClient.indices().exists(ex -> ex.index(INDEX_NAME));
        if (index_exists.value()) {
            logger.info("Index '" + INDEX_NAME + "' already exists");
            return;
        }
        elasticsearchClient.indices().create(c -> c.index(INDEX_NAME).mappings(m -> m
                .properties("id", p -> p.text(d -> d.fielddata(true)))
                .properties("header", p -> p.text(d -> d.fielddata(true)))
                .properties("text", p -> p.text(d -> d.fielddata(true)))
                .properties("summary", p -> p.text(d -> d.fielddata(true)))
                .properties("URL", p -> p.text(d -> d.fielddata(true)))
                .properties("date", p -> p.text(d -> d.fielddata(true)))
                .properties("time", p -> p.text(d -> d.fielddata(true)))
                .properties("place", p -> p.text(d -> d.fielddata(true)))
                .properties("hash", p -> p.text(d -> d.fielddata(true)))
        ));
        logger.info("Index '" + INDEX_NAME + "' was created!");
    }

    public void insertData(NewsEntity newsEntity) {
        try {
            IndexResponse response = elasticsearchClient.index(i -> i.index(INDEX_NAME).document(newsEntity));
            logger.info("Page from: " + newsEntity.getURL() + " was added to elastic");
        } catch (IOException e) {
            logger.error("Error with inserting page to elastic from: " + newsEntity.getURL());
            logger.error(e.getMessage());
        }

    }

    public boolean checkExistence(String hash) {
        SearchResponse<NewsEntity> response = null;
        try {
            response = elasticsearchClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(q -> q
                                    .match(t -> t
                                            .field("hash")
                                            .query(hash)
                                    )
                            ),
                    NewsEntity.class
            );
        } catch (IOException e) {
            logger.error("Error with check existence for -> " + hash);
            System.exit(1);
        }
        return response.hits().total().value() != 0;
    }

    public void ExecuteSearchRequests() throws IOException {

        Query byDateMatch = MatchQuery.of(m -> m
                .field("date")
                .query("14 мая")
        )._toQuery();

        Query byBodyPekinMatch = MatchQuery.of(m -> m
                .field("place")
                .query("Пекин")
        )._toQuery();

        Query byHeaderMoscowMatch = MatchQuery.of(m -> m
                .field("header")
                .query("встреча")
        )._toQuery();

        Query byAuthorTermQuery = new Query.Builder().term(t -> t
                .field("place")
                .value(v -> v.stringValue("Кремль"))
        ).build();

        // AND

        SearchResponse<NewsEntity> andResponse = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .bool(b -> b
                                        .must(byBodyPekinMatch, byDateMatch)//, byHeaderSevastopolMatch)
                                )
                        ),
                NewsEntity.class
        );

        List<Hit<NewsEntity>> andHits = andResponse.hits().hits();
        outputHits(andHits);

        // OR
        SearchResponse<NewsEntity> orResponse = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .bool(b -> b
                                        .should(byBodyPekinMatch, byDateMatch)
                                )
                        ),
                NewsEntity.class
        );

        List<Hit<NewsEntity>> orHits = orResponse.hits().hits();
        outputHits(orHits);

        // SCRIPT
        SearchResponse<NewsEntity> scriptResponse = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .scriptScore(ss -> ss
                                        .query(q1 -> q1
                                                .matchAll(ma -> ma))
                                        .script(scr -> scr
                                                .inline(i -> i
                                                        .source("doc['time'].value.length()"))))),
                NewsEntity.class
        );

        List<Hit<NewsEntity>> scriptHits = scriptResponse.hits().hits();
        outputHits(scriptHits);


        // MULTIGET
        MgetResponse<NewsEntity> mgetResponse = elasticsearchClient.mget(mgq -> mgq
                        .index(INDEX_NAME)
                        .docs(d -> d
                                .id("TgoagY8BCG0Vgdhn4NDU")
                                .id("UgoagY8BCG0Vgdhn4tDA")),
                NewsEntity.class
        );
        List<NewsEntity> mgetHits = new ArrayList<>();

        mgetHits.add(mgetResponse.docs().get(0).result().source());
        for (NewsEntity newsEntity : mgetHits) {
            assert newsEntity != null;
            logger.info("Found headline. Place: " + newsEntity.getPlace() + " Summary: " + newsEntity.getSummary() +
                    " URL: " + newsEntity.getURL() + " Header: " + newsEntity.getHeader());
        }
        System.out.println();


        // Histogram Aggregation
        Aggregation agg3 = Aggregation.of(a -> a.histogram(dha -> dha.script(scr -> scr
                        .inline(i -> i
                                .source("doc['header'].value.length()")
                                .lang("painless"))
                ).interval(10.0)
        ));
        SearchResponse<?> hAggregation = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .aggregations("header_length_histogram", agg3),
                NewsEntity.class
        );

        logger.info(String.valueOf(hAggregation));

        // Terms Aggregation
        Aggregation agg4 = Aggregation.of(a -> a.terms(t -> t
                        .field("author")
                )
        );
        SearchResponse<?> tAggregation = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .aggregations("popular_authors", agg4),
                NewsEntity.class
        );

        logger.info(String.valueOf(tAggregation));

        // Filter Aggregation
        Aggregation agg5_1 = Aggregation.of(a -> a
                .avg(avg -> avg
                        .script(scr -> scr
                                .inline(i -> i
                                        .source("doc['summary'].value.length()")
                                        .lang("painless"))
                        )
                )
        );
        Aggregation agg5 = Aggregation.of(a -> a
                .filter(q -> q.term(t -> t
                                .field("place")
                                .value("Пекин")
                        )
                )
                .aggregations("avg_body_length", agg5_1)
        );
        SearchResponse<?> fAggregation = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .aggregations("filtered_body", agg5),
                NewsEntity.class
        );

        logger.info(String.valueOf(fAggregation));

//        Logs Aggregation
        Aggregation agg6 = Aggregation.of(a -> a.terms(t -> t
                        .field("stream.keyword")
                        .size(10)
                )
        );
        SearchResponse<?> lAggregation = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .aggregations("streams", agg6)
                        .size(0),
                NewsEntity.class
        );

        logger.debug(String.valueOf(lAggregation));
        System.out.println();
    }
    private void outputHits(List<Hit<NewsEntity>> hits) {
        if (hits.isEmpty()) {
            logger.info("Empty response");
        }
        for (Hit<NewsEntity> hit: hits) {
            NewsEntity newsEntity = hit.source();
            assert newsEntity != null;
            logger.info("Found headline. Place: " + newsEntity.getPlace() + " Summary: " + newsEntity.getSummary() +
                    " URL: " + newsEntity.getURL() + " Header: " + newsEntity.getHeader());
        }
    }
}
