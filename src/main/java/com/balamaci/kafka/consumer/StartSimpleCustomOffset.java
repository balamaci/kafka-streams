package com.balamaci.kafka.consumer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author sbalamaci
 */
public class StartSimpleCustomOffset {

    private static final Config config = ConfigFactory.load("app");

    private static final String CONSUMER_GROUP_ID = "consumer-logs-processing";

    public static void main(String[] args) {
        int numConsumers = 1;

        List<String> topics = Arrays.asList(config.getString("kafka.topics"));
        ExecutorService executor = Executors.newFixedThreadPool(numConsumers);


        final List<SimpleConsumerCustomOffset> consumers = new ArrayList<>();
        for (int i = 1; i <= numConsumers; i++) {
            SimpleConsumerCustomOffset consumer = new SimpleConsumerCustomOffset(i, 2,
                    topics, properties(CONSUMER_GROUP_ID));
            consumers.add(consumer);

            executor.submit(consumer);
        }

        registerShutdownHook(executor, consumers);
    }

    private static void registerShutdownHook(ExecutorService executor, List<SimpleConsumerCustomOffset> consumers) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (SimpleConsumerCustomOffset consumer : consumers) {
                    consumer.shutdown();
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static Properties properties(String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka.bootstrap.servers"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        //
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5");

        return props;
    }

}
