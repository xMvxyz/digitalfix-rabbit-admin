package com.digitalfix.rabbitadmin.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

import com.digitalfix.rabbitadmin.config.RabbitMQTopology;
import com.digitalfix.rabbitadmin.dto.QueueStatus;

@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public void createQueue(String queueName) {
        rabbitAdmin.declareQueue(QueueBuilder.durable(queueName).build());
        log.info("Cola declarada: {}", queueName);
    }

    public void createExchange(String exchangeName, String type) {
        Exchange exchange = switch (type.toLowerCase()) {
            case "direct" -> new DirectExchange(exchangeName, true, false);
            case "topic" -> new TopicExchange(exchangeName, true, false);
            case "fanout" -> new FanoutExchange(exchangeName, true, false);
            case "headers" -> new HeadersExchange(exchangeName, true, false);
            default -> throw new IllegalArgumentException("Tipo de exchange invalido: " + type);
        };
        rabbitAdmin.declareExchange(exchange);
        log.info("Exchange declarado: {} (tipo {})", exchangeName, type.toLowerCase());
    }

    public void createBinding(String queueName, String exchangeName, String routingKey) {
        Binding binding = BindingBuilder
                .bind(new Queue(queueName))
                .to(new DirectExchange(exchangeName))
                .with(routingKey);
        rabbitAdmin.declareBinding(binding);
        log.info("Binding declarado: {} -[{}]-> {}", exchangeName, routingKey, queueName);
    }

    public QueueStatus getQueueStatus(String queueName) {
        QueueInformation info = rabbitAdmin.getQueueInfo(queueName);
        if (info == null) {
            return null;
        }
        return new QueueStatus(info.getName(), queueName.endsWith(".dlq"),
                info.getMessageCount(), info.getConsumerCount());
    }

    public List<QueueStatus> listDomainQueues() {
        return Stream.of(
                RabbitMQTopology.EMAIL_QUEUE, RabbitMQTopology.DISPATCH_QUEUE, RabbitMQTopology.REPORT_QUEUE,
                RabbitMQTopology.EMAIL_DLQ, RabbitMQTopology.DISPATCH_DLQ, RabbitMQTopology.REPORT_DLQ)
                .map(this::getQueueStatus)
                .filter(Objects::nonNull)
                .toList();
    }

    public boolean deleteQueue(String queueName) {
        boolean deleted = rabbitAdmin.deleteQueue(queueName);
        log.info("Cola eliminada: {} (existia: {})", queueName, deleted);
        return deleted;
    }

    public boolean deleteExchange(String exchangeName) {
        boolean deleted = rabbitAdmin.deleteExchange(exchangeName);
        log.info("Exchange eliminado: {} (existia: {})", exchangeName, deleted);
        return deleted;
    }

    public void deleteBinding(String queueName, String exchangeName, String routingKey) {
        Binding binding = BindingBuilder
                .bind(new Queue(queueName))
                .to(new DirectExchange(exchangeName))
                .with(routingKey);
        rabbitAdmin.removeBinding(binding);
        log.info("Binding eliminado: {} -[{}]-> {}", exchangeName, routingKey, queueName);
    }

    public void purgeQueue(String queueName) {
        rabbitAdmin.purgeQueue(queueName);
        log.info("Cola purgada: {}", queueName);
    }
}
