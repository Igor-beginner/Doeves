package md.brainet.doeves.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ApiError(

        @JsonProperty("path")
        String path,

        @JsonProperty("content")
        String message,

        @JsonProperty("status_code")
        int statusCode,

        @JsonProperty("date")
        LocalDateTime localDateTime
) {
}
