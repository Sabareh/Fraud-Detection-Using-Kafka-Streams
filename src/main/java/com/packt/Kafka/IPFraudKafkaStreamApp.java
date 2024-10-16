package com.packt.Kafka;
import com.packt.Kafka.lookup.CacheIPLookup;
import com.packt.Kafka.utils.PropertyReader;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.StreamsBuilder;
import java.util.Properties;

public class IPFraudKafkaStreamApp {
    private static CacheIPLookup cacheIPLookup = new CacheIPLookup();
    private static PropertyReader propertyReader = new PropertyReader();

    public static void main(String[] args) throws Exception {
        Properties KafkaStreamProperties = new Properties();
        KafkaStreamProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "IP-Fraud-Detection");
        KafkaStreamProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        KafkaStreamProperties.put(StreamsConfig.ZOOKEPER_CONNECT_CONFIG, "localhost:2181");
        KafkaStreamProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        KafkaStreamProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        Serde<String> stringSerde = Serdes.String();
        KStreamBuilder fraudDetectionTopology = new KStreamBuilder();
        KStream<String, String> ipRecords = fraudDetectionTopology.Stream(stringSerde, stringSerde, propertyReader.getPropertyValue("topic"));
        KStream<String, String> fraudIpRecords = ipRecords.filter((k, v) -> isFraud(v));
        fraudIpRecords.to(propertyReader.getPropertyValue("output_topic"));
        KafkaStreams StreamManager = new KafkaStreams(fraudDetectionTopology, KafkaStreamProperties);
        StreamManager.start();
        Runtime.getRuntime().addShutdownHook(new Thread(StreamManager::close));
    }
    private static boolean isFraud(String record) {
        String IP = record.split(" ")[0];
        String ranges = IP.split("\\.");
        String range = null;
        try {
            range = ranges[0] + "." + ranges[1];
        } catch (ArrayIndexOutOfBoundsException ex) {
            //handling here
        }
        return cacheIPLookup.isFraudIP(range);
    }
}