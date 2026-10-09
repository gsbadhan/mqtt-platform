package com.kafkapubsub.kafka;

import com.common.dto.ConfirmedMqttMessage;
import com.kafkapubsub.config.KafkaProducerConfig;
import com.common.config.KafkaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class Producer {
    private static final Logger log = LoggerFactory.getLogger(Producer.class);
    private KafkaProducerConfig producerConfig;
    private KafkaTemplate<String, ConfirmedMqttMessage> confirmedMqttMessageKafkaTemplate;
    private KafkaProperties kafkaProperties;

    @Autowired
    public Producer(KafkaProperties kafkaProperties, KafkaProducerConfig producerConfig,
                    @Qualifier("confirmedMqttMessageKafkaTemplate") KafkaTemplate<String, ConfirmedMqttMessage> confirmedMqttMessageKafkaTemplate) {
        this.kafkaProperties = kafkaProperties;
        this.producerConfig = producerConfig;
        this.confirmedMqttMessageKafkaTemplate = confirmedMqttMessageKafkaTemplate;
    }

    /**
     * Enqueue confirmed MQTT message for next level processing.
     *
     * @param message
     */
    public void publish(ConfirmedMqttMessage message) {
        if(kafkaProperties.getTopics().get(producerConfig.getConfirmedMqttMessageTopic()).getProducer().isEnabled()) {
            log.info("kafka publish clientId={}, packetId={}, topic={}", message.getClientId(), message.getPacketId(),
                    message.getTopic());
            confirmedMqttMessageKafkaTemplate.send(this.producerConfig.getConfirmedMqttMessageTopic(), "00", message);
        }
    }
}
