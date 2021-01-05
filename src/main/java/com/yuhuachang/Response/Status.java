package com.yuhuachang.Response;

public enum Status {
    OK("200 OK"), SWITCH_PROTOCOLS("101 Switching Protocols"), NOT_FOUND("404 Not Found");

    private String value;

    Status(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
