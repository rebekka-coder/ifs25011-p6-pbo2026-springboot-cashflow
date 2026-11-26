package org.delcom.ifs25011p6pbo2026springbootcashflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    @Test
    void appIsSpringBootApplication() {
        assertThat(App.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }
}
