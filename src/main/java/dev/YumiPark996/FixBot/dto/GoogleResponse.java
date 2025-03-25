package dev.YumiPark996.FixBot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleResponse(List<Item> items) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(String title, String link, String snippet) {}
}
