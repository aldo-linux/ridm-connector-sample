package com.ridm.connid.connector;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.identityconnectors.framework.common.objects.*;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.*;

public class SyncHandler {

    KafkaTemplate kafkaTemplate;

    public SyncHandler(KafkaTemplate kafkaTemplate){
        this.kafkaTemplate=  kafkaTemplate;
    }

    private final List<SyncDelta> results = new ArrayList<>();

//    SimpleProducer kafka = new SimpleProducer();

    private static final String TOPIC = "test-topic";


    private final SyncResultsHandler handler = new SyncResultsHandler() {
        public boolean handle(SyncDelta delta) {
            results.add(delta);
            return true;
        }
    };

    public SyncResultsHandler getHandler(){
        return handler;
    }

    public List<SyncDelta> getResults() {
        return results;
    }

    private final SyncResultsHandler saveKafkaHandler = new SyncResultsHandler() {
        public boolean handle(SyncDelta delta) {
            //            String uid = delta.getUid().getUidValue();
            Set<Attribute> attrs = delta.getObject().getAttributes();
            Map<String,Object> account = new HashMap<>();
            Map<String,Object> fields = new HashMap<>();
            for(Attribute att: attrs){
                fields.put(att.getName(),att.getValue().get(0));
            }
            account.put("account", fields);
            String message = JSONValue.toJSONString(account);
            kafkaTemplate.send(TOPIC, message);
            results.add(delta);
            // Producer producer = kafka.getProducer();
            // producer.send(new ProducerRecord<String, String>("test-topic",
            //         "key", delta.toString()));
            // System.out.println("Message sent successfully");
            // producer.close();
//            producer.sendMessage(delta.toString());
            return true;
        }
    };

    public SyncResultsHandler getSaveKafkaHandler() {
        return saveKafkaHandler;
    }
}
