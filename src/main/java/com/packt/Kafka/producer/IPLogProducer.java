package com.packt.Kafka.producer;
import com.packt.Kafka.utils.PropertyReader;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Future;

public class IPLogProducer extends TimerTask{
    public BufferedReader readFile() {
        return new BufferedReader(new
                InputStreamReader(
                Objects.requireNonNull(this.getClass().getResourceAsStream("/IP_LOG.log"))));
    }
    public static void main(final String[] args){
        Timer timer = new Timer();
        timer.schedule(new IPLogProducer(), 3000, 3000);
    }

    private String getNewRecordWithRandomIP(String line){
        Random r = new Random();
        String ip = r.nextInt(256) + "." + r.nextInt(256) + "." +
                r.nextInt(256) + "." + r.nextInt(256);
        String[] columns;
        columns = lines.Split(" ");
        columns[0] = ip;
        return Arrays.toString(columns);
    }

    @Override
    public void run() {
        PropertyReader propertyReader = new PropertyReader();
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers")
        propertyReader.getPropertyValue("broker.list");
        producerProps.put("key.serializer",
                "org.apache.Kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer",
                "org.apache.Kafka.common.serialization.StringSerializer");
        producerProps.put("auto.create.topics.enable", "true");
            KafkaProducer<String, String> ipProducer = new KafkaProducer<String, String>(producerProps);
            BufferedReader br = readFile();
            String oldLine = "";
            try {
                while ((oldLine = br.readLine()) != null) {
                    String line =
                            getNewRecordWithRandomIP(oldLine).replace("[", "").replace("]", "");
                    ProducerRecord<String, String> ipData = new ProducerRecord<String, String>(propertyReader.getPropertyValue("topic"), line);
                    Future<RecordMetadata> recordMetadata = ipProducer.send(ipData);}
            } catch (IOException e) {
                e.printStackTrace();
            }
            ipProducer.close();
    }
}
