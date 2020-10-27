package pt.smartthought.url.shortner.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UrlDto {
    @NotNull
    @NotEmpty
    @Size(min = 1)
    private String url;
}
