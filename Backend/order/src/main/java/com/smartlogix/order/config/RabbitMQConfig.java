package com.smartlogix.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "smartlogix.exchange";
    public static final String QUEUE_FAILED     = "order.failed.queue";
    public static final String QUEUE_CONFIRMED  = "order.confirmed.queue";
    public static final String QUEUE_SHIPPED    = "order.shipped.queue";
    public static final String QUEUE_DELIVERED  = "order.delivered.queue";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue orderFailedQueue() {
        return QueueBuilder.durable(QUEUE_FAILED)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_FAILED + ".dlq")
                .build();
    }

    @Bean
    public Queue orderConfirmedQueue() {
        return QueueBuilder.durable(QUEUE_CONFIRMED)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_CONFIRMED + ".dlq")
                .build();
    }

    @Bean
    public Queue orderShippedQueue() {
        return QueueBuilder.durable(QUEUE_SHIPPED)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_SHIPPED + ".dlq")
                .build();
    }

    @Bean
    public Queue orderDeliveredQueue() {
        return QueueBuilder.durable(QUEUE_DELIVERED)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_DELIVERED + ".dlq")
                .build();
    }

    @Bean public Queue orderFailedDlq()    { return new Queue(QUEUE_FAILED    + ".dlq", true); }
    @Bean public Queue orderConfirmedDlq() { return new Queue(QUEUE_CONFIRMED + ".dlq", true); }
    @Bean public Queue orderShippedDlq()   { return new Queue(QUEUE_SHIPPED   + ".dlq", true); }
    @Bean public Queue orderDeliveredDlq() { return new Queue(QUEUE_DELIVERED + ".dlq", true); }

    @Bean
    public Binding orderFailedBinding(Queue orderFailedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderFailedQueue).to(exchange).with("order.reservation.failed");
    }

    @Bean
    public Binding orderConfirmedBinding(Queue orderConfirmedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderConfirmedQueue).to(exchange).with("order.reservation.confirmed");
    }

    @Bean
    public Binding orderShippedBinding(Queue orderShippedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderShippedQueue).to(exchange).with("order.shipped");
    }

    @Bean
    public Binding orderDeliveredBinding(Queue orderDeliveredQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderDeliveredQueue).to(exchange).with("order.delivered");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.smartlogix.*");
        converter.setJavaTypeMapper(typeMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
