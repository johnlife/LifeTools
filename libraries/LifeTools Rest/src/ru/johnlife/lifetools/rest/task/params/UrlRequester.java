package ru.johnlife.lifetools.rest.task.params;

import com.android.volley.Request;

/**
 * Created by yanyu on 4/24/2016.
 */
public class UrlRequester extends AbstractUrlencodingRequester {

    public UrlRequester(String url, Object[][] params) {
        super(Request.Method.GET, url, params);
    }

    @Override
    public String getUrl() {
        return new StringBuilder(super.getUrl())
            .append("?")
            .append(encodeParams())
            .toString();
    }

    @Override
    public String getBody() {
        return null;
    }
}
