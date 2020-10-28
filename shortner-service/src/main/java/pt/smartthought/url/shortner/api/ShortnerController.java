package pt.smartthought.url.shortner.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pt.smartthought.url.shortner.api.dto.ShortUrlResponse;
import pt.smartthought.url.shortner.api.dto.UrlDto;
import pt.smartthought.url.shortner.config.AppDeploymentConfig;
import pt.smartthought.url.shortner.domain.ShortUrl;
import pt.smartthought.url.shortner.domain.ShortUrlRepository;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ShortnerController {

    private final HttpServletResponse response;
    private final AppDeploymentConfig config;
    private final ShortUrlRepository repository;

    @RequestMapping(value = "/{code:.*}",
            method = RequestMethod.GET)
    public ResponseEntity getUrl(@PathVariable String code) {

        UUID uuid = null;
        try {
            uuid = UUID.fromString(code);
        } catch(IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ShortUrl shorUrl = repository.get(uuid);
        if(shorUrl != null) {
            response.addHeader(HttpHeaders.LOCATION, shorUrl.getOriginalUrl());
            return new ResponseEntity<>(HttpStatus.FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces ="application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public ShortUrlResponse shortUrl(@Validated @RequestBody UrlDto url) {

        ShortUrl shortUrl = ShortUrl.generateNew(url.getUrl());
        repository.save(shortUrl);

        return ShortUrlResponse.builder()
                .url(config.buildUrl(shortUrl.getShortUrl()))
                .build();
    }


}
