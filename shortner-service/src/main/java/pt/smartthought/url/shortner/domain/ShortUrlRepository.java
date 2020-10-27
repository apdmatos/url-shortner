package pt.smartthought.url.shortner.domain;

import java.util.UUID;

/**
 * Repository interface to manage the urls
 */
public interface ShortUrlRepository {
    /**
     * Saves the short url into the database
     * @param shortUrl structure with short and original url
     */
    void save(ShortUrl shortUrl);

    /**
     * Gets the original url from the short one
     * @param shortUrl the short url
     * @return the original url to redirect to or null if it does not find any match
     */
    ShortUrl get(UUID shortUrl);
}
