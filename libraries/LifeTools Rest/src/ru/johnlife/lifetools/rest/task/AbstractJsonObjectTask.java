package ru.johnlife.lifetools.rest.task;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import org.json.JSONObject;

import ru.johnlife.lifetools.rest.task.misc.DecoratedRequest;
import ru.johnlife.lifetools.rest.task.params.Requester;


public abstract class AbstractJsonObjectTask<Value> extends AbstractRestTask<Value, JSONObject> {
	public class JsonObjectRequest extends DecoratedRequest<JSONObject> {
		public JsonObjectRequest(Requester request, Listener<JSONObject> listener, ErrorListener errorListener) {
			super(request, listener, errorListener);
		}

		@Override
		protected JSONObject processResponseString(String responseString) throws Exception {
			return new JSONObject(responseString);
		}
	}

	public AbstractJsonObjectTask(Context context, OnTaskCompleteListener<Value> listener) {
		super(context, listener);
	}

	@Override
	protected Request createRequest(Requester request, Listener<JSONObject> listener, ErrorListener errorListener) {
		return new JsonObjectRequest(request, listener, errorListener);
	}
}
