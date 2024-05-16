package gg.bmstu.utils;

import gg.bmstu.services.PagePublisher;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class RequestUtils {
    private static final int DELAY = 5000;
    private static final int TRYING_COUNT = 3;
    public static final String QUEUE_LINK = "crawler_link";
    public static final String QUEUE_PAGE = "crawler_page";
    private static final Logger logger = LoggerFactory.getLogger(RequestUtils.class);
    public static Optional<Document> request(String url) {
        Optional<Document> doc = Optional.empty();
        for (int tryIndex = 0; tryIndex < TRYING_COUNT; tryIndex++) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                final HttpGet httpGet = new HttpGet(url);
                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    int status_code = response.getStatusLine().getStatusCode();
                    switch (status_code) {
                        case 200: {
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                try {
                                    doc = Optional.ofNullable(Jsoup.parse(entity.getContent(), "UTF-8", url));
                                    logger.info("[*] Thread " +
                                            Thread.currentThread().getId() +
                                            " - Received Webpage from: " + url);
                                    return doc;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                        case 403:
                        case 429:
                        {
                            logger.info("[*] Thread " +
                                    Thread.currentThread().getId() +
                                    " - Error at " +
                                    url +
                                    " status code " +
                                    status_code);
                            int delay = DELAY * (tryIndex + 1);
                            try {
                                response.close();
                                httpClient.close();
                                logger.info("[!] sleep for " + delay / 1000 + "sec");
                                Thread.sleep(delay);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                            break;
                        }
                        case 404:
                            logger.info("[*] Thread " + Thread.currentThread().getId() + " - get 404 NOT FOUND for " + url);
                            break;
                        default:
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return doc;
    }
}
