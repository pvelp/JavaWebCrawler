package gg.bmstu.services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import gg.bmstu.utils.ElasticBridge;
import gg.bmstu.utils.RequestUtils;
import gg.bmstu.entity.NewsEntity;
import gg.bmstu.entity.UrlEntity;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class PagePublisher extends Thread {
    private final ConnectionFactory factory;
    private static final Logger logger = LoggerFactory.getLogger(PagePublisher.class);
    private final ElasticBridge elasticBridge;
    public PagePublisher(ConnectionFactory connectionFactory, ElasticBridge bridge) {
        factory = connectionFactory;
        elasticBridge = bridge;
    }


    @Override
    public void run() {
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            System.out.println("Connected to PAGE_QUEUE\n-Start parse");
            while (true) {
                synchronized (this) {
                    try {
                        if (channel.messageCount(RequestUtils.QUEUE_LINK) == 0) continue;
                        String jsonData = new String(channel.basicGet(RequestUtils.QUEUE_LINK, true)
                                .getBody(), StandardCharsets.UTF_8);
                        UrlEntity urlEntity = new UrlEntity();
                        urlEntity.objectFromStrJson(jsonData);
                        parse(urlEntity, channel);
                        notify();
                    } catch (IndexOutOfBoundsException e) {
                        wait();
                    }
                }
            }
        } catch (IOException | TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void parse(UrlEntity urlEntity, Channel channel) throws InterruptedException {
//        Thread.sleep(1000);
        if (elasticBridge.checkExistence(urlEntity.getHash())){
            System.out.println("URL: " + urlEntity.getUrl() + " was founded in ES. Hash: " + urlEntity.getHash());
            return;
        }
        String url = urlEntity.getUrl();
        Optional<Document> doc = RequestUtils.request(url);
        if (doc.isPresent()) {
            Document realDoc = doc.get();
            String header = realDoc.select("h1.entry-title.p-name").first().text();
            String summary = realDoc.select("div.read__lead.entry-summary.p-summary").first().text();
            String date = realDoc.select("time.read__published").text();
            String time = realDoc.select("div.read__time").text();
            String place = realDoc.select("div.read__place.p-location").text();

            StringBuilder text = new StringBuilder();
            Element div = realDoc.select("div.entry-content.e-content.read__internal_content").first();
            for (Element p : div.select("p")) {
                text.append(p.text()).append("\n");
            }

            NewsEntity newsEntity = new NewsEntity(
                    header,
                    text.toString(),
                    summary,
                    url,
                    date,
                    time,
                    place,
                    urlEntity.getHash()
            );
            try {
                channel.basicPublish("", RequestUtils.QUEUE_PAGE, null, newsEntity.toJsonString().getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

