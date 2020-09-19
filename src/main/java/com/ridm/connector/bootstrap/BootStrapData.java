package com.ridm.connector.bootstrap;

import com.ridm.connector.domain.Application;
import com.ridm.connector.domain.Connection;
import com.ridm.connector.repository.ApplicationRepository;
import com.ridm.connector.repository.ConnectionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class BootStrapData implements CommandLineRunner {

    private final ApplicationRepository applicationRepository;
    private final ConnectionRepository connectionRepository;

    public BootStrapData(ApplicationRepository applicationRepository, ConnectionRepository connectionRepository) {
        this.applicationRepository = applicationRepository;
        this.connectionRepository = connectionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Connection conn = new Connection("ldap", "ldap.tirasa.net.LDAPConnector.jar", "{prop1: something}");
        connectionRepository.save(conn);

        Application app = new Application( "app1", "This is an example of and LDAP app", conn);
        applicationRepository.save(app);

        System.out.println("Started bootstrap!");
        System.out.println("Number of connections: " + connectionRepository.count());
        System.out.println("Number of applications: " + applicationRepository.count());

        this.template.send("myTopic", "foo1");
        this.template.send("myTopic", "foo2");
        this.template.send("myTopic", "foo3");
        latch.await(60, TimeUnit.SECONDS);
        logger.info("All received");
    }

    //////
    public static Logger logger = Logger.getLogger(String.valueOf(BootStrapData.class));

    @Autowired
    private KafkaTemplate<String, String> template;

    private final CountDownLatch latch = new CountDownLatch(3);

    @KafkaListener(topics = "myTopic")
    public void listen(ConsumerRecord<?, ?> cr) throws Exception {
        logger.info(cr.toString());
        latch.countDown();
    }
}
