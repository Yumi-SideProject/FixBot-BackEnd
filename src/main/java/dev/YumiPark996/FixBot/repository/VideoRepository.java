package dev.YumiPark996.FixBot.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.YumiPark996.FixBot.dto.VideoDTO;
import dev.YumiPark996.FixBot.dto.VideoParam;
import dev.YumiPark996.FixBot.dto.VideoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Repository
public class VideoRepository {

    @Value("${video.key}")
    private String API_KEY;

    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3/search";
    private final HttpClient httpClient = getHttpClient();
    private final ObjectMapper objectMapper = getObjectMapper();

    public HttpClient getHttpClient() {
        return HttpClient.newHttpClient();
    }

    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    public String callAPI(VideoParam param) throws Exception {
        String encodedQuery = URLEncoder.encode(param.searchQuery(), StandardCharsets.UTF_8);  // ✅ URL 인코딩 적용
        String url = String.format(
                "%s?part=snippet&q=%s&key=%s&maxResults=5&type=video&order=relevance",
                BASE_URL, encodedQuery, API_KEY
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        }
        throw new RuntimeException("Failed : HTTP error code : " + response.statusCode());
    }

    public List<VideoDTO> getVideos(VideoParam param) throws Exception {
        String responseBody = callAPI(param);
        VideoResponse videoResponse = objectMapper.readValue(responseBody, VideoResponse.class);

        return videoResponse.items()
                .stream()
                .map(item -> new VideoDTO(
                        item.snippet().title(),
                        item.snippet().thumbnails().medium().url(),
                        "https://www.youtube.com/watch?v=" + item.id().videoId()
                ))
                .toList();
    }
}
