package com.yuhuachang.Response;

public enum Status {
    OK("200 OK");

    private String value;

    private Status(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
