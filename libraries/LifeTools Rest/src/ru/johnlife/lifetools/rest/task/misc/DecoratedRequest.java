package ru.johnlife.lifetools.rest.task.misc;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import java.util.Map;

import ru.johnlife.lifetools.rest.task.params.Requester;

/**
 * Created by yanyu on 5/2/2016.
 */
public abstract class DecoratedRequest<T> extends JsonRequest<T> {
	protected Requester request;

	public DecoratedRequest(Requester request, Response.Listener<T> listener, Response.ErrorListener errorListener) {
		super(request.getMethod(), request.getUrl(), request.getBody(), listener, errorListener);
		this.request = request;
	}

	@Override
    public String getBodyContentType() {
        String ct = request.getContentType();
        return (null == ct) ? super.getBodyContentType() : ct;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = request.getHeaders();
        return null == headers ? super.getHeaders() : headers;
    }

    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String responseString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(processResponseString(responseString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    protected abstract T processResponseString(String responseString) throws Exception;


}
