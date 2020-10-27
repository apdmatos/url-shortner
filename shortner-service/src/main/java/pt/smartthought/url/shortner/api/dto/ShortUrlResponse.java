package pt.smartthought.url.shortner.api.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class ShortUrlResponse {
    @NotNull
    private String url;
}
