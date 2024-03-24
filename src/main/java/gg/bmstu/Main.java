package gg.bmstu;

import gg.bmstu.service.WebCrawler;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;


public class Main {

    private static final String url = "https://www.mk.ru/news/";

    public static Document GetUrl(String url) {
        int code = 0;
        Document doc = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == 200) {
                    System.out.println("Status: " + statusLine.getStatusCode() + " OK " + Thread.currentThread().getName());
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        try {
                            doc = Jsoup.parse(entity.getContent(), "UTF-8", url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("error get url" + url + " code " + code);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return doc;
    }

    static void parseStartDoc(Document doc){
        var parentEl = doc.select(".news-listing__day-list");
        System.out.println(parentEl);
//        var testels = parentEl.select("li:nth-child(1)");
//        for (var elArticle : testels){
//            System.out.println(elArticle.getElementsByTag("h1").get(0).text());
//        }

    }

    public static void main(String[] args) {
//        System.out.println(parseStartDoc(GetUrl(url)));
//        parseStartDoc(GetUrl(url));
        WebCrawler webCrawler = new WebCrawler("http://www.kremlin.ru/events/president/news");
        try {
            webCrawler.getThread().join();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }



}