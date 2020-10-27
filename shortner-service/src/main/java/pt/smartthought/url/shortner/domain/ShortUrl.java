package pt.smartthought.url.shortner.domain;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ShortUrl {
    private UUID shortUrl;
    private String originalUrl;

    private ShortUrl() { }
    public ShortUrl(UUID shortUrl, String originalUrl) {
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
    }

    public static ShortUrl generateNew(String originalUrl) {
        return new ShortUrl(UUID.randomUUID(), originalUrl);
    }

}
