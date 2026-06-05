package com.smartlogix.eureka;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class EurekaServerApplicationTest {

    @Test
    void applicationClassDeclaresSpringBootAndEurekaServerAnnotations() {
        assertThat(EurekaServerApplication.class.getAnnotation(SpringBootApplication.class)).isNotNull();
        assertThat(EurekaServerApplication.class.getAnnotation(EnableEurekaServer.class)).isNotNull();
    }

    @Test
    void mainDelegatesToSpringApplicationRun() {
        String[] args = {"--server.port=0"};
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            springApplication.when(() -> SpringApplication.run(EurekaServerApplication.class, args))
                    .thenReturn(context);

            EurekaServerApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(EurekaServerApplication.class, args));
        }
    }
}
