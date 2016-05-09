package ru.johnlife.lifetools.rest.task;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import ru.johnlife.lifetools.rest.task.params.Requester;

public abstract class AbstractRestTask<DataType, ResponseType> {
	private static RequestQueue queue = null;

	private final Response.ErrorListener errorListener = new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			handleError(error);
		}
	};

	protected void handleError(VolleyError error) {
		if (error.getCause() != null) {
            if (error.getCause().getMessage() != null) {
                fail(error.getCause().getMessage());
            }
        } else if (null != error.getMessage()) {
            fail(new String(error.getMessage()));
        } else if (null != error.networkResponse) {
            fail("HTTP " + error.networkResponse.statusCode);
        } else {
            fail("Unknown error");
        }
	}

	private OnTaskCompleteListener<DataType> listener;

	public AbstractRestTask(Context context, OnTaskCompleteListener<DataType> listener) {
		if (null == queue) {
			queue = Volley.newRequestQueue(context);
		}
		this.listener = listener;
	}

	public void execute() {
		queue.add(createRequest(
			getRequest(),
			new Response.Listener<ResponseType>() {
				@Override
				public void onResponse(ResponseType response) {
					try {
						DataType result = processResponse(response);
						if (null != result && null != listener) {
							listener.success(result);
						}
					} catch (Exception e) {
						handleException(e);
					}
				}
			}, errorListener));
	}

	protected abstract Request createRequest(
			Requester request,
			com.android.volley.Response.Listener<ResponseType> listener,
			com.android.volley.Response.ErrorListener errorListener);

	protected abstract Requester getRequest();
	protected abstract DataType processResponse(ResponseType response) throws Exception;

	protected void handleException(Exception e) {
		Log.w(getClass().getName(), e);
		fail(e.getMessage());
	}

	protected void fail(String errorMessage) {
		if (null != listener) {
			listener.error(errorMessage);
		}
	}
}
