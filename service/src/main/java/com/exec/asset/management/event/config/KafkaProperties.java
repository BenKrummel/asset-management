/*
 * Copyright © 2022 Blue Yonder Group, Inc. ALL RIGHTS RESERVED.
 * This software is the confidential information of Blue Yonder Group, Inc., and is licensed as restricted rights software.
 * The use, reproduction, or disclosure of this software is subject to restrictions set forth in your license agreement with Blue Yonder.
 */

package com.exec.asset.management.event.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;

/**
 * This is a base class from which different broker specific kafka
 * properties classes may extend.
 */
@Getter
@Setter
public abstract class KafkaProperties {
    private Properties kafka;

    /**
     * Constructs a ProducerConfig object that may be used when create
     * a ProducerFactory.
     * @return a new ProducerConfig object.
     */
    public ProducerConfig buildProducerConfig() {
        return new ProducerConfig(kafka);
    }

}
