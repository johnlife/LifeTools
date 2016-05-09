package ru.johnlife.lifetools.rest.task.params;

import com.android.volley.Request;

/**
 * Created by yanyu on 5/2/2016.
 */
public class FormDataRequester extends AbstractUrlencodingRequester {

    public FormDataRequester(String url, Object[][] params) {
        super(Request.Method.POST, url, params);
    }

    @Override
    public String getContentType() {
        return "application/x-www-form-urlencoded; charset=utf-8";
    }

    @Override
    public String getBody() {
        return encodeParams();
    }
}
