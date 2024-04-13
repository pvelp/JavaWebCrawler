package gg.bmstu.services;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Optional;

public class RequestUtils {
    private static final int DELAY = 5000;
    private static final int TRYING_COUNT = 3;
    public static final String QUEUE_LINK = "crawler_link";
    public static final String QUEUE_PAGE = "crawler_page";
    public static Optional<Document> request(String url) {
        Optional<Document> doc = Optional.empty();
        for (int tryIndex = 0; tryIndex < TRYING_COUNT; tryIndex++) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                final HttpGet httpGet = new HttpGet(url);
                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    int status_code = response.getStatusLine().getStatusCode();
                    System.out.println("Code " + status_code);
                    switch (status_code) {
                        case 200: {
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                try {
                                    doc = Optional.ofNullable(Jsoup.parse(entity.getContent(), "UTF-8", url));
                                    System.out.println("[*] Thread " +
                                            Thread.currentThread().getId() +
                                            " - Received Webpage from: " + url);
                                    return doc;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                        case 403: {
                            System.out.println("[*] Thread " +
                                    Thread.currentThread().getId() +
                                    " - Error at " +
                                    url +
                                    " status code " +
                                    status_code);
                            int delay = DELAY * tryIndex;
                            try {
                                response.close();
                                httpClient.close();
                                Thread.sleep(delay);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            }
                            break;
                        }
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
