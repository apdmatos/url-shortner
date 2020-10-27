package pt.smartthought.url.shortner.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@ConfigurationProperties(prefix = "app-deployment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppDeploymentConfig {
    private String baseUrl;

    public String buildUrl(UUID shortCode) {
        return baseUrl + shortCode.toString();
    }
}
