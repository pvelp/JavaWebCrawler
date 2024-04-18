package gg.bmstu.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import gg.bmstu.entity.NewsEntity;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class PagePublisher extends Thread {
    private final ConnectionFactory factory;
    private final int averageThemesCount = 2;
    private final int averagePersonsCount = 2;

    public PagePublisher(ConnectionFactory connectionFactory) {
        factory = connectionFactory;
    }


    @Override
    public void run() {
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            ObjectMapper mapper = new ObjectMapper();

            while (true) {
                synchronized (this) {
                    try {
                        if (channel.messageCount(RequestUtils.QUEUE_LINK) == 0) continue;
                        String url = new String(channel.basicGet(RequestUtils.QUEUE_LINK, true)
                                .getBody(), StandardCharsets.UTF_8);

                        parse(url, mapper, channel);
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

    void parse(String url, ObjectMapper mapper, Channel channel) {
        Optional<Document> doc = RequestUtils.request(url);
        String jsonNewsEntity;
        if (doc.isPresent()) {
            Document realDoc = doc.get();
            String header = realDoc.select("h1.entry-title.p-name").first().text();
            String summary = realDoc.select("div.read__lead.entry-summary.p-summary").first().text();
            String date = realDoc.select("time.read__published").text();
            String time = realDoc.select("time.read__time").toString();
            String place = realDoc.select("div.read__place.p-location").text();

            StringBuilder text = new StringBuilder();
            Element div = realDoc.select("div.entry-content.e-content.read__internal_content").first();
            for (Element p : div.select("p")) {
                text.append(p.text()).append("\n");
            }

            List<String> themes = new ArrayList<>(averagePersonsCount);
            div = realDoc.select("div.read__tags.masha-ignore").first();
            for (Element li : div.select("li.p-category")) {
                themes.add(li.select("a").text());
            }

            List<String> persons = new ArrayList<>(averageThemesCount);
            for (Element li : div.select("li")) {
                if (li.attributes().get("href").contains("persons")) {
                    persons.add(li.select("a").text());
                }
            }
            NewsEntity newsEntity = new NewsEntity(
                    header,
                    text.toString(),
                    summary,
                    url,
                    date,
                    time,
                    place,
                    themes,
                    persons
            );

            try {
                jsonNewsEntity = mapper.writeValueAsString(newsEntity);
                channel.basicPublish("", RequestUtils.QUEUE_PAGE, null, jsonNewsEntity.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println(jsonNewsEntity);
        }
    }

}
