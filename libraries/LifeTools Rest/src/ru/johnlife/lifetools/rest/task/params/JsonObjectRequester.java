package ru.johnlife.lifetools.rest.task.params;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yanyu on 4/24/2016.
 */
public class JsonObjectRequester extends Requester{

    private final JSONObject json;

    public JsonObjectRequester(int method, String url, Object[][] params) {
        super(method, url, null);
        json = new JSONObject();
        for (Object[] param : params) {
            try {
                json.put(param[0].toString(), param[1]);
            } catch (JSONException e) {
                Log.d("JsonObjectRequester", "JSONException while creating params", e);
            }
        }
    }

    public JsonObjectRequester(int method, String url, JSONObject json) {
        super(method, url, null);
        this.json = json;
    }

    @Override
    public String getBody() {
        return json == null ? null : json.toString();
    }
}
