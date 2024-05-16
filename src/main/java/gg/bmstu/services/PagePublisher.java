package gg.bmstu.services;

import com.rabbitmq.client.*;
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
            logger.info("Connected to rabbit mq page queue for publish");
            while (true) {
//                synchronized (this) {
                try {
                    if (channel.messageCount(RequestUtils.QUEUE_LINK) == 0) continue;
//                        GetResponse response = channel.basicGet(RequestUtils.QUEUE_LINK, true);
                    channel.basicConsume(RequestUtils.QUEUE_LINK, false, "javaConsumerTag", new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body)
                                throws IOException {
                            long deliveryTag = envelope.getDeliveryTag();
                            String message = new String(body, StandardCharsets.UTF_8);
                            UrlEntity urlEntity = new UrlEntity();
                            urlEntity.objectFromStrJson(message);
                            try {
                                parse(urlEntity, channel);
                            } catch (InterruptedException e) {
                                logger.info(e.getMessage());
                            }
                            channel.basicAck(deliveryTag, false);
                        }
                    });

//                        long devTag = response.getEnvelope().getDeliveryTag();
//                        String jsonData = new String(response.getBody(), StandardCharsets.UTF_8);
//                        UrlEntity urlEntity = new UrlEntity();
//                        urlEntity.objectFromStrJson(jsonData);
//                        parse(urlEntity, channel);
//                        channel.basicAck(devTag, true);
//                        notify();
                } catch (IndexOutOfBoundsException e) {
//                        wait();
                    logger.info(e.getMessage());
                }
//                }
            }
//        } catch (IOException | TimeoutException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    void parse(UrlEntity urlEntity, Channel channel) throws InterruptedException {
//        Thread.sleep(1000);
        if (elasticBridge.checkExistence(urlEntity.getHash())){
            logger.info("[!] URL: " + urlEntity.getUrl() + " was founded in elastic. Hash: " + urlEntity.getHash());
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
                logger.info("Publish page in page queue");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

