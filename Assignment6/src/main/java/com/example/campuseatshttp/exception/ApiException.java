package com.example.campuseatshttp.exception;

public class ApiException extends RuntimeException {
    private final String type;
    private final String title;
    private final int status;

    public ApiException(String type, String title, int status, String detail) {
        super(detail);
        this.type = type;
        this.title = title;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }
}
