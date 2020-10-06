package com.ridm.connid.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class SimpleProducer {

    private static final String TOPIC = "test-topic";
//
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//
//    public void sendMessage(String message) {
//        kafkaTemplate.send(TOPIC, message);
//    }
//
//    public static void main(String[] args) {
//        String message = "Hello";
//        Producer prod = new Producer();
//        prod.sendMessage(message);
//    }
    public Producer getProducer(){
        Properties props = new Properties();

        props.put("bootstrap.servers", "192.168.0.4:9092");
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("value.serializer",
//                "io.confluent.kafka.serializers.KafkaJsonSerializer");

        props.put("retries", 0);

        props.put("client.id", "demo1");

        Producer<String, String> producer = new KafkaProducer
                <String, String>(props);



        return producer;
    }

    public KafkaConsumer getConsumer(){
        Properties props = new Properties();

        props.put("bootstrap.servers", "192.168.0.4:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");

//        props.put("value.deserializer",
//                "io.confluent.kafka.serializers.KafkaJsonDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer
                <String, String>(props);

        return consumer;
    }

    public static void main(String[] args) {
        SimpleProducer kafka = new SimpleProducer();

        Producer producer = kafka.getProducer();

        Map<Object, Object> json = new HashMap<>();

        json.put("name", "Hector");
        json.put("lastName", "Gomez");

        String jsonFinal = JSONValue.toJSONString(json);

        producer.send(new ProducerRecord<String, Object>(TOPIC,
                "key", jsonFinal));
        System.out.println("Message sent successfully");
        producer.close();

        KafkaConsumer consumer = kafka.getConsumer();

        consumer.subscribe(Arrays.asList(TOPIC));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                // print the offset,key and value for the consumer records.
                // System.out.println("AQUI ESTOY");
                System.out.printf("offset = %d, key = %s, value = %s\n",
                        record.offset(), record.key(), record.value());
                Object objConnection = JSONValue.parse(record.value());
                JSONObject jsonObject = (JSONObject) objConnection;
                System.out.println("JSON: " + jsonObject + " " + jsonObject.toJSONString() + " " + jsonObject.entrySet() + " " +jsonObject.get("name") + " " + jsonObject.size());
            }
        }

    }


}
