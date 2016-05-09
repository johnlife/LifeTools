package ru.johnlife.lifetools.rest.task;

import android.content.Context;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetools.rest.task.misc.DecoratedRequest;
import ru.johnlife.lifetools.rest.task.params.Requester;


public abstract class AbstractJsonArrayTask<Item> extends AbstractRestTask<List<Item>, JSONArray> {

	private class JsonArrayRequest extends DecoratedRequest<JSONArray> {

		public JsonArrayRequest(Requester request, Listener<JSONArray> listener, ErrorListener errorListener) {
			super(request, listener, errorListener);
		}

		@Override
		protected JSONArray processResponseString(String responseString) throws Exception {
			return getArray(responseString);
		}
	}

	protected JSONArray getArray(String responseString) throws JSONException {
		return new JSONArray(responseString);
	}

	public AbstractJsonArrayTask(Context context, OnTaskCompleteListener<List<Item>> listener) {
		super(context, listener);
	}

	@Override
	protected JsonRequest<JSONArray> createRequest(Requester request, Listener<JSONArray> listener, ErrorListener errorListener) {
		return new JsonArrayRequest(request, listener, errorListener);
	}
	
	@Override
	protected List<Item> processResponse(JSONArray array) throws Exception {
		int size = array.length();
		List<Item> value = new ArrayList<Item>();
		for (int i=0; i<size; i++) {
			value.add(createItem(array.getJSONObject(i)));
		}
		return value;
	}

	protected abstract Item createItem(JSONObject json) throws JSONException;

}
