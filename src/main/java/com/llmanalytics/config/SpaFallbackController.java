package com.llmanalytics.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class SpaFallbackController {

    @GetMapping(value = "/{path:^(?!api|actuator|assets|index\\.html).*}/**")
    public String forward() {
        return "forward:/index.html";
    }
    
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}
