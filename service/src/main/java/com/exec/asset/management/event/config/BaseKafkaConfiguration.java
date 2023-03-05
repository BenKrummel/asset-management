/*
 * Copyright © 2022 Blue Yonder Group, Inc. ALL RIGHTS RESERVED.
 * This software is the confidential information of Blue Yonder Group, Inc., and is licensed as restricted rights software.
 * The use, reproduction, or disclosure of this software is subject to restrictions set forth in your license agreement with Blue Yonder.
 */

package com.exec.asset.management.event.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Contains shared kafka related configuration properties and logic
 */
@Configuration
@EnableConfigurationProperties(BaseKafkaConfiguration.Kafka.class)
@ConditionalOnProperty(value = "asset.kafka.bootstrap.servers")
public class BaseKafkaConfiguration {

    protected final KafkaProperties kafkaProps;

    /**
     * Constructs a new configuration instance.
     * @param kafkaProps kafka properties.
     */
    public BaseKafkaConfiguration(Kafka kafkaProps) {
        this.kafkaProps = kafkaProps;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(kafkaProps.buildProducerConfig().originals());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @ConfigurationProperties(prefix = "asset")
    public static class Kafka extends KafkaProperties {

    }
}
