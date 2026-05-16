package com.smartlogix.eureka;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EurekaServerApplicationTest {

    @Test
    void applicationClassIsAvailable() {
        assertThat(EurekaServerApplication.class).isNotNull();
    }
}
