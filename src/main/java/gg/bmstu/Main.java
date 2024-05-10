package gg.bmstu;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import gg.bmstu.services.LinkPublisher;
import gg.bmstu.services.PagePublisher;
import gg.bmstu.services.PageReciever;
import gg.bmstu.services.RequestUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class Main {

    private static final String url = "http://www.kremlin.ru/events/president/news";

    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setVirtualHost("/");
        factory.setUsername("rabbitmq");
        factory.setPassword("rabbitmq");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(RequestUtils.QUEUE_LINK, false, false, false, null);
        channel.queueDeclare(RequestUtils.QUEUE_PAGE, false, false, false, null);
        channel.close();
        connection.close();

        LinkPublisher linkPublisher = new LinkPublisher(url, factory);
        PagePublisher pagePublisher = new PagePublisher(factory);
        PageReciever pageReciever = new PageReciever(factory);

        linkPublisher.start();
        pagePublisher.start();
        pageReciever.start();

        linkPublisher.join();
        pagePublisher.join();
        pageReciever.join();

    }



}