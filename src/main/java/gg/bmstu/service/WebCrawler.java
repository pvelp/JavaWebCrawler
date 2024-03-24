package gg.bmstu.service;

import gg.bmstu.entity.NewsEntity;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class WebCrawler implements Runnable {
    private static final int MAX_DEPTH = 3;
    private final Thread thread;
    private final String link;
    UUID crawler_id;
    public WebCrawler(String link){
        this.link = link;
        crawler_id = UUID.randomUUID();
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        crawl(link);
    }

    private void crawl(String url){
        Optional<Document> doc = request(url);
        if (doc.isPresent()){
            Document realDoc = doc.get();
            ArrayList<NewsEntity> newsEntities = new ArrayList<>();
            for (Element div : realDoc.select("div.entry-content.lister-page")){
                System.out.println(div);
            }
        }
    }

    private Optional<Document> request(String url){
        Optional<Document> doc = Optional.empty();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()){
            final HttpGet httpGet = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)){
                int status_code = response.getStatusLine().getStatusCode();
                switch (status_code) {
                    case 200: {
                        System.out.println("Crawler ID: " + crawler_id + " Received Webpage at " + url);
                        HttpEntity entity = response.getEntity();

                        if (entity != null) {
                            try {
                                doc = Optional.ofNullable(Jsoup.parse(entity.getContent(), "UTF-8", url));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                    case 400: {
                        System.out.println("Crawler ID: " + crawler_id + " Error at " + url + " status code "
                                + status_code);
                        break;
                    }
                }
            }
            return doc;
        } catch (IOException e){
            return Optional.empty();
        }
    }

    public Thread getThread(){
        return thread;
    }
}
