package com.smartlogix.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME          = "smartlogix.exchange";
    public static final String QUEUE_ORDER_CREATED    = "notification.queue";
    public static final String QUEUE_ORDER_SHIPPED    = "notification.shipped.queue";
    public static final String QUEUE_ORDER_CONFIRMED  = "notification.confirmed.queue";
    public static final String QUEUE_ORDER_REJECTED   = "notification.rejected.queue";
    public static final String QUEUE_ORDER_DELIVERED  = "notification.delivered.queue";

    @Bean public TopicExchange exchange() { return new TopicExchange(EXCHANGE_NAME); }

    @Bean public Queue orderCreatedQueue()   { return new Queue(QUEUE_ORDER_CREATED,   true); }
    @Bean public Queue orderShippedQueue()   { return new Queue(QUEUE_ORDER_SHIPPED,   true); }
    @Bean public Queue orderConfirmedQueue() { return new Queue(QUEUE_ORDER_CONFIRMED, true); }
    @Bean public Queue orderRejectedQueue()  { return new Queue(QUEUE_ORDER_REJECTED,  true); }
    @Bean public Queue orderDeliveredQueue() { return new Queue(QUEUE_ORDER_DELIVERED, true); }

    @Bean public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(exchange).with("order.created");
    }
    @Bean public Binding orderShippedBinding(Queue orderShippedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderShippedQueue).to(exchange).with("order.shipped");
    }
    @Bean public Binding orderConfirmedBinding(Queue orderConfirmedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderConfirmedQueue).to(exchange).with("order.reservation.confirmed");
    }
    @Bean public Binding orderRejectedBinding(Queue orderRejectedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderRejectedQueue).to(exchange).with("order.reservation.failed");
    }
    @Bean public Binding orderDeliveredBinding(Queue orderDeliveredQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderDeliveredQueue).to(exchange).with("order.delivered");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
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
