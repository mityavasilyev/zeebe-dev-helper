package io.github.mityavasilyev.zeebedevhelper.dto;

import lombok.Getter;

@Getter
public class SuccessResponse<T> extends AbstractResponse<T> {

    public SuccessResponse(T responseData) {
        super(Status.SUCCESS, responseData);
    }
}
