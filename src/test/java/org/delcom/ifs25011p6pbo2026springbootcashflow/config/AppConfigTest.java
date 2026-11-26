package org.delcom.ifs25011p6pbo2026springbootcashflow.config;

import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.ai.OpenAiFinanceService;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IFinanceAiService;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.FinanceAiUseCase;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigTest {

    @Test
    void wiresAllBeans(@TempDir Path dir) throws Exception {
        DataSource ds = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        AppConfig config = new AppConfig(dir.resolve("uploads").toString());

        IUserRepository users = config.userRepository(ds);
        ITransactionRepository txs = config.transactionRepository(ds);
        IFinanceAiService ai = config.financeAiService("http://x/v1/", "k", "m");
        AuthUseCase auth = config.authUseCase(users);
        UserUseCase userUseCase = config.userUseCase(users);
        FinanceUseCase finance = config.financeUseCase(txs);
        FinanceAiUseCase financeAi = config.financeAiUseCase(ai, finance);
        FinancePresenter presenter = config.financePresenter();
        FileStorage storage = config.fileStorage();

        assertThat(users).isInstanceOf(JdbcUserRepository.class);
        assertThat(txs).isInstanceOf(JdbcTransactionRepository.class);
        assertThat(ai).isInstanceOf(OpenAiFinanceService.class);
        assertThat(userUseCase).isNotNull();
        assertThat(financeAi).isNotNull();
        assertThat(presenter).isNotNull();

        ApplicationRunner runner = config.seedRootAdmin(auth, "admin", "admin");
        runner.run(null);
        assertThat(users.findByUsername("admin")).isPresent();

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);
        config.addResourceHandlers(registry);
        verify(registration).addResourceLocations(storage.directory().toUri().toString());
        assertThat(storage.directory()).isDirectory();
    }
}
