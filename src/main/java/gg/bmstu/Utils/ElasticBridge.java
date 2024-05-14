package gg.bmstu.Utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.rabbitmq.client.ConnectionFactory;
import gg.bmstu.entity.NewsEntity;
import gg.bmstu.services.PageReciever;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticConnector {
    private final ObjectMapper mapper;
    private final RestClient restClient;
    private final ElasticsearchTransport elasticsearchTransport;
    private final ElasticsearchClient elasticsearchClient;
    private final String INDEX_NAME;
    private final static Logger logger = LoggerFactory.getLogger(PageReciever.class);

    public ElasticConnector(String elUrl, String indexName) {
        mapper = JsonMapper.builder().build();
        restClient = RestClient
                .builder(HttpHost.create(elUrl)).build();
        elasticsearchTransport = new RestClientTransport(restClient,
                new JacksonJsonpMapper(mapper));
        elasticsearchClient = new ElasticsearchClient(elasticsearchTransport);
        INDEX_NAME = indexName;
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
        ));
        logger.info("Index '" + INDEX_NAME + "' was created!");
    }

    public void insertData(NewsEntity newsEntity) {
        try {
            IndexResponse response = elasticsearchClient.index(i -> i.index(INDEX_NAME).document(newsEntity));
        } catch (IOException e) {
            logger.error("Error with inserting page to elastic from: " + newsEntity.getURL());
            logger.error(e.getMessage());
            return;
        }
        logger.info("Page from: " + newsEntity.getURL() + " was added to elastic");
    }

    public boolean checkExistence(String uuid) {
        SearchResponse<NewsEntity> response = null;
        try {
            response = elasticsearchClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(q -> q
                                    .match(t -> t
                                            .field("id")
                                            .query(uuid)
                                    )
                            ),
                    NewsEntity.class
            );
        } catch (IOException e) {
            logger.error("Error with check existence for -> " + uuid);
            System.exit(1);
        }
        return response.hits().total().value() != 0;
    }
}
