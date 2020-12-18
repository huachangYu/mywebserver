package com.yuhuachang.Response;

public enum ContentType {
    TXT("text/plain"), HTML("text/html");

    private String value;

    private ContentType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Content-type: " + this.value;
    }

}
