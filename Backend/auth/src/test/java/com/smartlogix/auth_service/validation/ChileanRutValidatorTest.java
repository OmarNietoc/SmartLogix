package com.smartlogix.auth_service.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChileanRutValidatorTest {

    @Test
    void normalize_removesFormattingAndUppercasesVerifier() {
        assertThat(ChileanRutValidator.normalize("76.123.456-0")).isEqualTo("761234560");
    }

    @Test
    void normalize_rejectsNullMalformedAndInvalidVerifier() {
        assertThatThrownBy(() -> ChileanRutValidator.normalize(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorio");
        assertThatThrownBy(() -> ChileanRutValidator.normalize("abc"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ChileanRutValidator.normalize("76.123.456-8"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
