package ru.johnlife.lifetools.rest.task.params;

import android.net.Uri;

/**
 * Created by yanyu on 5/2/2016.
 */
public abstract class AbstractUrlencodingRequester extends Requester {

    public AbstractUrlencodingRequester(int method, String url, Object[][] params) {
        super(method, url, params);
    }

    protected String encodeParams() {
        Object[][] params = getParams();
        if (null == params) return null;
        StringBuilder b = new StringBuilder();
        for (Object[] param : params) {
            b.append(Uri.encode(param[0].toString()))
                    .append('=')
                    .append(Uri.encode(param[1].toString()))
                    .append('&');
        }
        return b.delete(b.length()-1, b.length()).toString();
    }
}
