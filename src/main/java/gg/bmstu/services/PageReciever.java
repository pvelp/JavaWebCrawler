package gg.bmstu.services;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class PageReciever extends Thread {
    private final ConnectionFactory factory;

    public PageReciever(ConnectionFactory factory) {
        this.factory = factory;
    }

    @Override
    public void run() {
        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            Client client = new PreBuiltTransportClient(
                    Settings.builder().put("cluster.name", "docker-cluster").build())
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

            while (true) {
                    try {
                        if (channel.messageCount(RequestUtils.QUEUE_PAGE) == 0) continue;
                        String jsonNewsEntity = new String(channel.basicGet(RequestUtils.QUEUE_PAGE, true)
                                .getBody(), StandardCharsets.UTF_8);

                        String sha256hex = org.apache.commons.codec.digest.DigestUtils.sha256Hex(jsonNewsEntity);
                        client.prepareIndex("pages", "_doc", sha256hex)
                                .setSource(jsonNewsEntity, XContentType.JSON)
                                .get();
                    } catch (IndexOutOfBoundsException e) {
                        throw new RuntimeException(e);
                    }
            }

        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

}
