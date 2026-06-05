package com.smartlogix.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void exchange_returnsTopicExchangeWithCorrectName() {
        TopicExchange exchange = config.exchange();
        assertThat(exchange.getName()).isEqualTo(RabbitMQConfig.EXCHANGE_NAME);
    }

    @Test
    void queue_returnsDurableQueueWithCorrectName() {
        Queue queue = config.queue();
        assertThat(queue.getName()).isEqualTo(RabbitMQConfig.QUEUE_NAME);
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    void inventoryDlq_returnsDurableDeadLetterQueue() {
        Queue dlq = config.inventoryDlq();
        assertThat(dlq.getName()).isEqualTo(RabbitMQConfig.QUEUE_NAME + ".dlq");
        assertThat(dlq.isDurable()).isTrue();
    }

    @Test
    void binding_bindsQueueToExchangeWithRoutingKey() {
        Queue queue = config.queue();
        TopicExchange exchange = config.exchange();

        Binding binding = config.binding(queue, exchange);

        assertThat(binding.getExchange()).isEqualTo(RabbitMQConfig.EXCHANGE_NAME);
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.ROUTING_KEY);
    }

    @Test
    void messageConverter_returnsJackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = config.messageConverter(new ObjectMapper());
        assertThat(converter).isNotNull();
    }

    @Test
    void rabbitTemplate_returnsConfiguredTemplate() {
        ConnectionFactory cf = mock(ConnectionFactory.class);
        Jackson2JsonMessageConverter converter = config.messageConverter(new ObjectMapper());

        RabbitTemplate template = config.rabbitTemplate(cf, converter);

        assertThat(template).isNotNull();
    }
}
