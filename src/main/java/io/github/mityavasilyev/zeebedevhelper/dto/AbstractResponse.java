package io.github.mityavasilyev.zeebedevhelper.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@Getter
public class AbstractResponse<T> {

    private final URI requestPath = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
    private final Status status;
    private final T responseData;

    public static enum Status {
        SUCCESS, ERROR
    }
}
