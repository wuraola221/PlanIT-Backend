package PlanIT.PlanIT.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String baseUrl = "http://localhost:8080";
    private Mail mail = new Mail();

    @Data
    public static class Mail {
        private String from = "oyemadewura@gmail.com";
    }
}
