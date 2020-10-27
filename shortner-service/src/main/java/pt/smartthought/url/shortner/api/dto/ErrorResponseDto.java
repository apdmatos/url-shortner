package pt.smartthought.url.shortner.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class ErrorResponseDto {

    public static ErrorResponseDto INVALID_REQUEST = ErrorResponseDto.builder()
            .message("Invalid request")
            .build();

    public static ErrorResponseDto INVALID_MEDIA_TYPE = ErrorResponseDto.builder()
            .message("Invalid versions of response requested (content-type)")
            .build();

    public static ErrorResponseDto INVALID_ACCEPTS = ErrorResponseDto.builder()
            .message("Invalid version of request supplied (accept)")
            .build();

    public static ErrorResponseDto UNEXPECTED_ERROR = ErrorResponseDto.builder()
            .message("Unexpected Error")
            .build();

    private String message;
}
