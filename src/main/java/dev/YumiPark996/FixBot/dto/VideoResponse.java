package dev.YumiPark996.FixBot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VideoResponse(List<Item> items) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(Id id, Snippet snippet) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Id(String videoId) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snippet(String title, Thumbnails thumbnails) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnails(Medium medium) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Medium(String url) {}
}
