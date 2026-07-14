package com.mybf.utils;

import java.util.Objects;

public class UrlMethod {
    private String url;
    private String method;

    public UrlMethod(){}

    public UrlMethod(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    private String capitalize(String str){
        return str.toUpperCase();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        UrlMethod urlMethod = (UrlMethod) object;
        return Objects.equals(url, urlMethod.url) && Objects.equals(capitalize(method),capitalize(urlMethod.method));
    }
}
