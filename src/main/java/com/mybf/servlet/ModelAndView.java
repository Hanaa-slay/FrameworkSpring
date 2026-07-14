package com.mybf.servlet;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private Map<String, Object> attributes;
    private String url;

    public ModelAndView(String url){
        attributes = new HashMap<>();
        this.url = url;
    }

    public void setAttribute(String key, Object value){
        attributes.put(key, value);
    }

    public Object getAttribute(String key){
        return attributes.get(key);
    }

    public Map<String, Object> getListAttributes(){
        return this.attributes;
    }

    public String getUrl(){
        return this.url;
    }
}
