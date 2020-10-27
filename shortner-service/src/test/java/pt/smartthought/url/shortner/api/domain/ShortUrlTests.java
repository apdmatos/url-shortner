package pt.smartthought.url.shortner.api.domain;

import org.junit.jupiter.api.Test;
import pt.smartthought.url.shortner.domain.ShortUrl;

import static org.junit.jupiter.api.Assertions.*;

public class ShortUrlTests {

    @Test
    public void test_shortcode_generation() {
        ShortUrl shortUrl = ShortUrl.generateNew("http://localhost:8080");

        assertNotNull(shortUrl.getOriginalUrl());
        assertNotNull(shortUrl.getShortUrl());
        assertNotEquals(shortUrl.getOriginalUrl(), "");
        assertNotEquals(shortUrl.getShortUrl(), "");
    }

    @Test
    public void test_shortcode_generates_different_codes() {
        ShortUrl shortUrl = ShortUrl.generateNew("http://localhost:8080");
        ShortUrl shortUrl2 = ShortUrl.generateNew("http://localhost:8080");

        assertNotNull(shortUrl.getOriginalUrl());
        assertNotNull(shortUrl.getShortUrl());

        assertNotEquals(shortUrl.getShortUrl(), shortUrl2.getShortUrl());
    }
}
