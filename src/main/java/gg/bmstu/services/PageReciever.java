package gg.bmstu.services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import gg.bmstu.utils.ElasticBridge;
import gg.bmstu.utils.RequestUtils;
import gg.bmstu.entity.NewsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PageReciever extends Thread {
    private final ConnectionFactory factory;
    private final ElasticBridge elasticBridge;
    private final static Logger logger = LoggerFactory.getLogger(PageReciever.class);

    public PageReciever(ConnectionFactory factory, ElasticBridge bridge) {
        this.factory = factory;
        elasticBridge = bridge;
    }

    @Override
    public void run() {
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            System.out.println("Connected to LINK_QUEUE\n-Start send to es");
            while (true) {
                try {
                    if (channel.messageCount(RequestUtils.QUEUE_PAGE) == 0) continue;
                    String jsonNewsEntity = new String(channel.basicGet(RequestUtils.QUEUE_PAGE, true)
                            .getBody(), UTF_8);
                    NewsEntity newsEntity = new NewsEntity();
                    newsEntity.objectFromStrJson(jsonNewsEntity);
                    if (!elasticBridge.checkExistence(newsEntity.getHash())) {
                        elasticBridge.insertData(newsEntity);
                    } else {
                        System.out.println("URL: " + newsEntity.getURL() + " was founded in ES. Hash: " + newsEntity.getHash());
                    }

                } catch (IndexOutOfBoundsException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}