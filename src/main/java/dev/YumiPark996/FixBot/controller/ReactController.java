package dev.YumiPark996.FixBot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ReactController {
    @RequestMapping({"/", "/chat", "/community", "/results"})
    public String serveReactApp() {
        return "forward:/index.html";  // 🚀 React 빌드된 index.html 반환
    }
}
