package com.fk.relocation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app.kafka-topics.security-topic")
public class SecurityTopicProperties {
    private String name;
    private Integer partitions;
    private Integer replicas;
    private Integer minInSyncReplicas;
}
