package com.medicine.auth.dto;

public class CommonResponse<T> {

    private final String message;
    private final T data;
    private final int status;

    public CommonResponse(String message, T data, int status) {
        this.message = message;
        this.data = data;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }
}
