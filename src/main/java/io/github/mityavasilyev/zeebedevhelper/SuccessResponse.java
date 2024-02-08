package io.github.mityavasilyev.zeebedevhelper;

import lombok.Getter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Getter
public class SuccessResponse<T> {
    private static final String SUCCESS_HEADER = "success";
    private final SuccessDTO<T> response;

    public SuccessResponse(SuccessDTO<T> response) {
        this.response = response;
    }

    public SuccessResponse(T data) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequestUri();
        this.response = new SuccessDTO<>(builder.build().toUri(), SUCCESS_HEADER, data);
    }

    public SuccessResponse(T data, String status) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequestUri();
        this.response = new SuccessDTO<>(builder.build().toUri(), status, data);
    }

    public SuccessResponse(String status, URI uri) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequestUri();
        this.response = new SuccessDTO<>(builder.build().toUri(), status, null);
    }

    public SuccessResponse(T data, String status, URI uri) {
        this.response = new SuccessDTO<>(uri, status, data);
    }

    public record SuccessDTO<T>(URI path, String status, T data) {

    }
}
