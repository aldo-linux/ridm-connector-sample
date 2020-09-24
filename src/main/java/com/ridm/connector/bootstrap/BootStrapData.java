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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.vladmihalcea.hibernate.type.json.internal.JacksonUtil;

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

    private Application application; ///this is to be used in the topic to avoid passing the application id

    @Override
    public void run(String... args) throws Exception {
        Connection conn = new Connection("ldap", "ldap.tirasa.net.LDAPConnector.jar", JacksonUtil.toJsonNode("{\"prop1\": \"something\"}"));
        connectionRepository.save(conn);

       Connection c = connectionRepository.findById(conn.getId()).get();

        System.out.println("Connection retrireved: " + c.getConfigProperties().asText());

        Application app = new Application( "app1", "This is an example of and LDAP app", conn);
        applicationRepository.save(app);
        application = app;

        // Create 5 base groups
        for(int j=1; j<=5; j++) {
            String groupValue = "{\"name\": \"grupito" + j + "\", \"description\":\"description"+j+"\"}";
            this.template.send("createGroup", groupValue);
        }

        // Create 5 accounts
        for(int i=1; i<=5; i++) {
            String accountValue = "{\"name\": \"pablito" + i + "\", \"mail\":\"pablito"+i+"@gmail.com\"}";
            this.template.send("createAccount", accountValue);
        }

        // Add 5 accounts (newly created) into each of the groups
        /*Iterable<Group> groupList = groupRepository.findAll();
        Iterable<Account> accountList = accountRepository.findAll();
        groupList.forEach(g -> {
            accountList.forEach(a -> {
                g.setAccount(a);
                groupRepository.save(g);
            });
        });*/

        System.out.println("Started bootstrap!");
        System.out.println("Number of connections: " + connectionRepository.count());
        System.out.println("Number of applications: " + applicationRepository.count());
        System.out.println("Number of accounts: " + accountRepository.count());
        System.out.println("Number of groups: " + groupRepository.count());

        latch.await(60, TimeUnit.SECONDS);
        logger.info("All received");
    }

    ////// KAFKA STUFF
    public static Logger logger = Logger.getLogger(String.valueOf(BootStrapData.class));

    @Autowired
    private KafkaTemplate<String, String> template;

    private final CountDownLatch latch = new CountDownLatch(3);

    @KafkaListener(topics = "createAccount")
    public void createAccount(ConsumerRecord<?, ?> cr) throws Exception {
        Account account = new Account(JacksonUtil.toJsonNode((String)cr.value()),application);
        accountRepository.save(account);
        logger.info(account.toString());
        latch.countDown();
    }

    @KafkaListener(topics = "createGroup")
    public void createGroup(ConsumerRecord<?, ?> cr) throws Exception {
        Group group = new Group(JacksonUtil.toJsonNode((String)cr.value()));
        groupRepository.save(group);
        logger.info(group.toString());
        latch.countDown();
    }

    @KafkaListener(topics = "addAccountToGroup")
    public void addAccountToGroup(ConsumerRecord<?, ?> cr) throws Exception {
        logger.info(cr.toString());
        latch.countDown();
    }
}
