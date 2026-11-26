package org.delcom.ifs25011p6pbo2026springbootcashflow.config;

import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.ai.OpenAiFinanceService;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IFinanceAiService;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.FinanceAiUseCase;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25011p6pbo2026springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/** Composition root: seluruh wiring bean ada di sini. */
@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public AppConfig(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    public ITransactionRepository transactionRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcTransactionRepository(dataSource);
    }

    @Bean
    public IFinanceAiService financeAiService(@Value("${OPENAI_ENDPOINT:}") String endpoint,
                                              @Value("${OPENAI_KEY:}") String key,
                                              @Value("${OPENAI_MODEL:}") String model) {
        return new OpenAiFinanceService(endpoint, key, model);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository users) {
        return new AuthUseCase(users);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository users) {
        return new UserUseCase(users);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactions) {
        return new FinanceUseCase(transactions);
    }

    @Bean
    public FinanceAiUseCase financeAiUseCase(IFinanceAiService ai, FinanceUseCase finance) {
        return new FinanceAiUseCase(ai, finance);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean
    public FileStorage fileStorage() {
        return new FileStorage(uploadDir);
    }

    @Bean
    public ApplicationRunner seedRootAdmin(AuthUseCase authUseCase,
                                           @Value("${app.root-username}") String rootUsername,
                                           @Value("${app.root-password}") String rootPassword) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    /** Layani file upload (foto profil & bukti transaksi) di /uploads/**. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var directory = fileStorage().directory();
        directory.toFile().mkdirs();
        registry.addResourceHandler("/uploads/**").addResourceLocations(directory.toUri().toString());
    }
}
