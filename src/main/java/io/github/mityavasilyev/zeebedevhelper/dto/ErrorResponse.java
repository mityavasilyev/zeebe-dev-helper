package io.github.mityavasilyev.zeebedevhelper.dto;

import lombok.Getter;

@Getter
public class ErrorResponse<T> extends AbstractResponse<T> {

    private Exception exception;

    public ErrorResponse(T responseData, Exception exception) {
        super(Status.ERROR, responseData);
        this.exception = exception;
    }

    public ErrorResponse(T responseData) {
        super(Status.ERROR, responseData);
    }
}
