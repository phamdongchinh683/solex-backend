package com.example.solex_backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE            = "solex.notifications";
    public static final String NEW_ORDER_QUEUE     = "solex.notification.new-order";
    public static final String ORDER_STATUS_QUEUE  = "solex.notification.order-status";
    public static final String NEW_ORDER_KEY       = "notification.new-order";
    public static final String ORDER_STATUS_KEY    = "notification.order-status";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue newOrderQueue() {
        return QueueBuilder.durable(NEW_ORDER_QUEUE).build();
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(ORDER_STATUS_QUEUE).build();
    }

    @Bean
    public Binding newOrderBinding(Queue newOrderQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(newOrderQueue).to(notificationExchange).with(NEW_ORDER_KEY);
    }

    @Bean
    public Binding orderStatusBinding(Queue orderStatusQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(orderStatusQueue).to(notificationExchange).with(ORDER_STATUS_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
