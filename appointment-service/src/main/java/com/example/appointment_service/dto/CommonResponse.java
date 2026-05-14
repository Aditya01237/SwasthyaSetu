package com.example.appointment_service.dto;

public class CommonResponse<T> {

    private String message;
    private T data;
    private int status;

    public CommonResponse(String message, T data, int status) {
        this.message = message;
        this.data = data;
        this.status = status;
    }

    public String getMessage() { return message; }
    public T getData() { return data; }
    public int getStatus() { return status; }
}
