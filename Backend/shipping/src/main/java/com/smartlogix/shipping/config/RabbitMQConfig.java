package com.smartlogix.shipping.config;

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
    public static final String CONFIRMED_QUEUE_NAME = "shipping.confirmed.queue";
    public static final String ROUTING_KEY_RESERVATION_CONFIRMED = "order.reservation.confirmed";
    public static final String ROUTING_KEY_ORDER_SHIPPED = "order.shipped";
    public static final String ROUTING_KEY_ORDER_DELIVERED = "order.delivered";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue shippingConfirmedQueue() {
        return QueueBuilder.durable(CONFIRMED_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CONFIRMED_QUEUE_NAME + ".dlq")
                .build();
    }

    @Bean
    public Queue shippingConfirmedDlq() {
        return new Queue(CONFIRMED_QUEUE_NAME + ".dlq", true);
    }

    @Bean
    public Binding shippingConfirmedBinding(Queue shippingConfirmedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(shippingConfirmedQueue).to(exchange).with(ROUTING_KEY_RESERVATION_CONFIRMED);
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
