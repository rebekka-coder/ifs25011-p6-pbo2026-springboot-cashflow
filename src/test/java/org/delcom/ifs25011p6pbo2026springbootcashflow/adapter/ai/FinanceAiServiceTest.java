package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinanceAiServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HttpServer server;
    private volatile int status = 200;
    private volatile String body = "";
    private final AtomicReference<String> lastRequest = new AtomicReference<>();
    private final AtomicReference<String> lastAuth = new AtomicReference<>();

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            lastRequest.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            lastAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort() + "/v1/";
    }

    private static String chat(String content) throws Exception {
        return MAPPER.writeValueAsString(Map.of("choices", List.of(Map.of("message", Map.of("content", content)))));
    }

    @Test
    void generatesTransactionsFromChatCompletion() throws Exception {
        body = chat("```json\n[{\"description\":\"Gaji\",\"amount\":5000000,\"type\":\"INCOME\"},"
                + "{\"description\":\"Makan\",\"amount\":\"25000\",\"type\":\"expense\"},"
                + "{\"description\":\"\",\"amount\":1,\"type\":\"INCOME\"},"
                + "{\"description\":\"Nol\",\"amount\":0,\"type\":\"INCOME\"},"
                + "{\"description\":\"Aneh\",\"amount\":5,\"type\":\"BAD\"}, 5]\n```");
        OpenAiFinanceService service = new OpenAiFinanceService(baseUrl(), "rahasia", "model-x");

        List<AiGeneratedTransaction> result = service.generateTransactions("gajian lalu makan");

        assertThat(result).containsExactly(
                new AiGeneratedTransaction("Gaji", 5_000_000L, TransactionType.INCOME),
                new AiGeneratedTransaction("Makan", 25_000L, TransactionType.EXPENSE));
        assertThat(lastAuth.get()).isEqualTo("Bearer rahasia");
        assertThat(lastRequest.get()).contains("model-x").contains("gajian lalu makan");
    }

    @Test
    void failsOnHttpError() {
        status = 500;
        body = "error";
        OpenAiFinanceService service = new OpenAiFinanceService(baseUrl(), "k", "m");
        DomainException e = assertThrows(DomainException.class, () -> service.generateTransactions("x"));
        assertThat(e).hasMessageContaining("500");
    }

    @Test
    void failsWhenServerUnreachable() {
        OpenAiFinanceService service = new OpenAiFinanceService("http://localhost:1", "k", "m");
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
    }

    @Test
    void failsOnInvalidEndpoint() {
        OpenAiFinanceService service = new OpenAiFinanceService("", "k", "m");
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
    }

    @Test
    void restoresInterruptFlag() {
        OpenAiFinanceService service = new OpenAiFinanceService(baseUrl(), "k", "m");
        Thread.currentThread().interrupt();
        assertThrows(DomainException.class, () -> service.generateTransactions("x"));
        assertThat(Thread.interrupted()).isTrue();
    }

    @Test
    void parseRejectsBadResponses() throws Exception {
        assertThrows(DomainException.class, () -> OpenAiFinanceService.parse("bukan json"));
        assertThrows(DomainException.class, () -> OpenAiFinanceService.parse(chat("tidak ada array")));
        assertThrows(DomainException.class, () -> OpenAiFinanceService.parse(chat("][")));
        assertThrows(DomainException.class, () -> OpenAiFinanceService.parse(chat("[ bukan json ]")));
        assertThat(OpenAiFinanceService.parse(chat("[]"))).isEmpty();
    }

    @Test
    void inMemoryStubIsDeterministic() {
        List<AiGeneratedTransaction> items = new InMemoryFinanceAiService().generateTransactions("tes");
        assertThat(items).hasSize(2);
        assertThat(items.get(0).type()).isEqualTo(TransactionType.INCOME);
        assertThat(items.get(1).type()).isEqualTo(TransactionType.EXPENSE);
    }
}
