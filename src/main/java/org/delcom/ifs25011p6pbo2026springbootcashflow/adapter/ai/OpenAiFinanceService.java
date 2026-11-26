package org.delcom.ifs25011p6pbo2026springbootcashflow.adapter.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25011p6pbo2026springbootcashflow.domain.repository.IFinanceAiService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Adapter HTTP ke API OpenAI-compatible (OPENAI_ENDPOINT / OPENAI_KEY / OPENAI_MODEL). */
public class OpenAiFinanceService implements IFinanceAiService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String SYSTEM_PROMPT = "Kamu asisten pencatat keuangan. Ubah teks pengguna menjadi "
            + "JSON array saja (tanpa penjelasan, tanpa markdown). Setiap elemen berbentuk "
            + "{\"description\": string, \"amount\": bilangan bulat rupiah positif, "
            + "\"type\": \"INCOME\" atau \"EXPENSE\"}.";

    private final String endpoint;
    private final String key;
    private final String model;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public OpenAiFinanceService(String endpoint, String key, String model) {
        this.endpoint = endpoint.replaceAll("/+$", "");
        this.key = key;
        this.model = model;
    }

    @Override
    public List<AiGeneratedTransaction> generateTransactions(String prompt) throws DomainException {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint + "/chat/completions"))
                    .timeout(Duration.ofSeconds(90))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + key)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody(prompt)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new DomainException("Layanan AI gagal (HTTP " + response.statusCode() + ")");
            }
            return parse(response.body());
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new DomainException("Layanan AI tidak dapat dihubungi: " + e.getMessage());
        }
    }

    private String requestBody(String prompt) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", model);
        root.put("temperature", 0.2);
        ArrayNode messages = root.putArray("messages");
        messages.addObject().put("role", "system").put("content", SYSTEM_PROMPT);
        messages.addObject().put("role", "user").put("content", prompt);
        return root.toString();
    }

    /** Mengambil JSON array transaksi dari respons chat-completions (toleran terhadap code fence). */
    static List<AiGeneratedTransaction> parse(String body) throws DomainException {
        try {
            String content = MAPPER.readTree(body).path("choices").path(0).path("message").path("content").asText("");
            int start = content.indexOf('[');
            int end = content.lastIndexOf(']');
            if (start < 0 || end < start) {
                throw new DomainException("Respons AI tidak berisi daftar transaksi");
            }
            List<AiGeneratedTransaction> result = new ArrayList<>();
            for (JsonNode node : MAPPER.readTree(content.substring(start, end + 1))) {
                toTransaction(node).ifPresent(result::add);
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new DomainException("Respons AI tidak valid: " + e.getOriginalMessage());
        }
    }

    private static Optional<AiGeneratedTransaction> toTransaction(JsonNode node) {
        String description = node.path("description").asText("").trim();
        long amount = node.path("amount").asLong(0);
        TransactionType type = switch (node.path("type").asText("").trim().toUpperCase(Locale.ROOT)) {
            case "INCOME" -> TransactionType.INCOME;
            case "EXPENSE" -> TransactionType.EXPENSE;
            default -> null;
        };
        if (description.isEmpty() || amount <= 0 || type == null) {
            return Optional.empty();
        }
        return Optional.of(new AiGeneratedTransaction(description, amount, type));
    }
}
