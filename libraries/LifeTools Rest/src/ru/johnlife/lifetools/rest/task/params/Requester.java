package ru.johnlife.lifetools.rest.task.params;

import java.util.Map;

/**
 * Created by yanyu on 4/24/2016.
 */
public abstract class Requester {
    private int method;
    private String url;
    private Object[][] params;

    public Requester(int method, String url, Object[][] params) {
        this.method = method;
        this.url = url;
        this.params = params;
    }

    protected Object[][] getParams() {
        return params;
    }

    public int getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public abstract String getBody();

    public String getContentType() {
        return null; //unspecified
    }

    public Map<String, String> getHeaders() {
        return null;
    }
}
