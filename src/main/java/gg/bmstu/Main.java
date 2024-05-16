package gg.bmstu;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import gg.bmstu.utils.ElasticBridge;
import gg.bmstu.services.LinkPublisher;
import gg.bmstu.services.PagePublisher;
import gg.bmstu.utils.RequestUtils;
import gg.bmstu.services.PageReciever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class Main {

    private static final String url = "http://www.kremlin.ru/events/president/news";
    private static final String INDEX_NAME = "pages";
    private static final String EL_URL = "http://localhost:9200";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {
        logger.info("Start service");
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

        ElasticBridge elasticBridge = new ElasticBridge(EL_URL, INDEX_NAME);
        elasticBridge.createIndex();

        LinkPublisher linkPublisher = new LinkPublisher(url, factory);
        PagePublisher pagePublisher = new PagePublisher(factory, elasticBridge);
        PageReciever pageReciever = new PageReciever(factory, elasticBridge);

        linkPublisher.start();
        pagePublisher.start();
        pageReciever.start();

        linkPublisher.join();
        pagePublisher.join();
        pageReciever.join();

        elasticBridge.ExecuteSearchRequests();

    }

}