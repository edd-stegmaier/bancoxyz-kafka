package cl.duoc.bancoxyz.transacciones.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import cl.duoc.bancoxyz.transacciones.dto.TransaccionEvento;

@EnableKafka
@Configuration
public class KafkaProducerConfig {

    public static final String TOPIC = "transacciones-bancoxyz";

    @Value("${app.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("bootstrap.servers", bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    NewTopic topicTransacciones() {
        Map<String, String> configs = new HashMap<>();
        configs.put("retention.ms", "43200000");
        // 3 particiones repartidas entre los 3 brokers, replica en cada uno
        return new NewTopic(TOPIC, 3, (short) 3).configs(configs);
    }

    @Bean
    ProducerFactory<String, TransaccionEvento> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,
                "org.apache.kafka.clients.producer.RoundRobinPartitioner");
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    KafkaTemplate<String, TransaccionEvento> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
