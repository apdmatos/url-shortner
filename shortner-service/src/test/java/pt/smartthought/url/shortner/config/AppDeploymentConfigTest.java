package pt.smartthought.url.shortner.config;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AppDeploymentConfigTest {

    @Test
    public void test_url_generation() {
        AppDeploymentConfig conf = new AppDeploymentConfig("http://localhost:8080/");
        UUID uuid = UUID.randomUUID();
        String url = conf.buildUrl(uuid);

        assertEquals(url, "http://localhost:8080/" + uuid.toString());
    }
}
