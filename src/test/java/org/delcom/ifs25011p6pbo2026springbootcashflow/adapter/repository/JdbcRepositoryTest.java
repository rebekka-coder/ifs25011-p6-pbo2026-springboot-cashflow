package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

class JdbcRepositoryTest extends RepositoryContractTest {

    private DataSource dataSource;

    @Override
    IUserRepository newUsers() {
        dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Override
    ITransactionRepository newTxs() {
        return new JdbcTransactionRepository(dataSource);
    }

    @Test
    void schemaInitializerIsIdempotent() {
        assertThatCode(() -> SchemaInitializer.initialize(dataSource)).doesNotThrowAnyException();
    }
}
