package dev.YumiPark996.FixBot.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public class FixBotPromptLoader {
    private static final String PROMPT_FILE_PATH = "config/fixbot_prompt.txt";

    public static String loadPrompt() {
        try {
            ClassLoader classLoader = FixBotPromptLoader.class.getClassLoader();
            URL resource = classLoader.getResource(PROMPT_FILE_PATH);

            if (resource == null) {
                System.err.println("❌ fixbot_prompt.txt 파일을 찾을 수 없습니다.");
                return "FixBot 기본 프롬프트를 불러올 수 없습니다.";
            }

            Path path = Paths.get(resource.toURI());
            return Files.readString(path, StandardCharsets.UTF_8);

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            return "FixBot 기본 프롬프트를 불러올 수 없습니다.";
        }
    }
}
