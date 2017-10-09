package com.nimasystems.lightcast.network;

public class Header {

    private String key;

    private String value;

    public Header() {
        //
    }

    public Header(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Header setValue(String value) {
        this.value = value;
        return this;
    }

    public String getValue() {
        return value;
    }
}
