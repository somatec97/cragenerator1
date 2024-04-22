package fr.nazza.cragenerator1.configuration;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import  org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.context.annotation.Configuration;
@Configuration

public class CraConfig {
    public void addCorsMapping(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

}
