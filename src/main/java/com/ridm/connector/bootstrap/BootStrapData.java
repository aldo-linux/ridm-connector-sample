package com.ridm.connector.bootstrap;

import com.ridm.connector.domain.Account;
import com.ridm.connector.domain.Application;
import com.ridm.connector.domain.Connection;
import com.ridm.connector.domain.Group;
import com.ridm.connector.repository.AccountRepository;
import com.ridm.connector.repository.ApplicationRepository;
import com.ridm.connector.repository.ConnectionRepository;

import com.ridm.connector.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class BootStrapData implements CommandLineRunner {

    private final ApplicationRepository applicationRepository;
    private final ConnectionRepository connectionRepository;
    private final AccountRepository accountRepository;
    private final GroupRepository groupRepository;

    public BootStrapData(ApplicationRepository applicationRepository, ConnectionRepository connectionRepository,
                         AccountRepository accountRepository, GroupRepository groupRepository) {
        this.applicationRepository = applicationRepository;
        this.connectionRepository = connectionRepository;
        this.accountRepository = accountRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Connection conn = new Connection("ldap", "ldap.tirasa.net.LDAPConnector.jar", "{prop1: something}");
        connectionRepository.save(conn);

        Application app = new Application( "app1", "This is an example of and LDAP app", conn);
        applicationRepository.save(app);

        // Create 5 base groups
        for(int j=1; j<=5; j++) {
            Group group = new Group("{\"name\": \"grupito" + j + "\", \"description\":\"description"+j+"\"}");
            groupRepository.save(group);
        }

        // Create 5 accounts
        for(int i=1; i<=5; i++) {
            Account account = new Account("{\"name\": \"pablito" + i + "\", \"mail\":\"pablito"+i+"@gmail.com\"}", app);
            accountRepository.save(account);
        }

        // Add 5 accounts (newly created) into each of the groups
        Iterable<Group> groupList = groupRepository.findAll();
        Iterable<Account> accountList = accountRepository.findAll();
        groupList.forEach(g -> {
            accountList.forEach(a -> {
                g.setAccount(a);
                groupRepository.save(g);
            });
        });

        System.out.println("Started bootstrap!");
        System.out.println("Number of connections: " + connectionRepository.count());
        System.out.println("Number of applications: " + applicationRepository.count());
        System.out.println("Number of accounts: " + accountRepository.count());
        System.out.println("Number of groups: " + groupRepository.count());

//        this.template.send("myTopic", "foo1");
//        this.template.send("myTopic", "foo2");
//        this.template.send("myTopic", "foo3");
//        latch.await(60, TimeUnit.SECONDS);
//        logger.info("All received");
    }
/*
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

 */
}
