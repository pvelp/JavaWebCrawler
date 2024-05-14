package gg.bmstu.services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import gg.bmstu.utils.RequestUtils;
import gg.bmstu.entity.UrlEntity;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class LinkPublisher extends Thread {
    private final String link;
    private final String BASE_URL = "http://www.kremlin.ru/";
    private final ConnectionFactory factory;
    public LinkPublisher(String link, ConnectionFactory connectionFactory) {
        this.link = link;
        factory = connectionFactory;
    }

    @Override
    public void run() {
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            System.out.println("Connected to LINK_QUEUE\n-Start crawl");
            crawl(link, channel);
            System.out.println("Finish crawl");
            channel.close();
            connection.close();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void crawl(String url, Channel channel) throws IOException {
        Optional<Document> doc = RequestUtils.request(url);
        if (doc.isPresent()) {
            Document realDoc = doc.get();
            for (Element div : realDoc.select("div.hentry.h-entry.hentry_event")) {
                for (Element a : div.children()
                        .select("a")
                        .not("a[aria-hidden=true")
                        .not("a.tabs_photo")
                        .not("a.tabs_video")
                        .not("a.tabs_article.item.big")) {
                    try {
                        String href = BASE_URL + a.attributes().get("href");
                        String title = div.select("span.entry-title.p-name").text();
                        UrlEntity urlEntity = new UrlEntity(href, title);
                        channel.basicPublish("", RequestUtils.QUEUE_LINK, null,
                                urlEntity.toJsonString().getBytes());
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        }
    }
}
