package dev.YumiPark996.FixBot.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.YumiPark996.FixBot.dto.GoogleDTO;
import dev.YumiPark996.FixBot.dto.GoogleParam;
import dev.YumiPark996.FixBot.dto.GoogleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Repository
public class GoogleRepository {
    @Value("${google.key}")
    private String API_KEY;

    @Value("${google.cs}")
    private String CX;

    private static final String BASE_URL = "https://www.googleapis.com/customsearch/v1";
    private final HttpClient httpClient = getHttpClient();
    private final ObjectMapper objectMapper = getObjectMapper();

    public HttpClient getHttpClient() {
        return HttpClient.newHttpClient();
    }

    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    public String callAPI(GoogleParam param) throws Exception {
        String encodedQuery = URLEncoder.encode(param.searchQuery(), StandardCharsets.UTF_8);  // ✅ URL 인코딩 적용

        String url = String.format(
                "%s?q=%s&key=%s&cx=%s&num=5",
                BASE_URL, encodedQuery, API_KEY, CX
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))  // ✅ 인코딩된 쿼리 사용
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        }
        throw new RuntimeException("Failed : HTTP error code : " + response.statusCode());
    }

    public List<GoogleDTO> getResults(GoogleParam param) throws Exception {
        String responseBody = callAPI(param);
        GoogleResponse googleResponse = objectMapper.readValue(responseBody, GoogleResponse.class);

        return googleResponse.items()
                .stream()
                .map(item -> new GoogleDTO(
                        item.title(),
                        item.link(),
                        item.snippet() // ✅ 누락된 snippet 추가
                ))
                .toList();
    }
}
