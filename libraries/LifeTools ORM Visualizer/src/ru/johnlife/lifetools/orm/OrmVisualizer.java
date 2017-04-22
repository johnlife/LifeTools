package ru.johnlife.lifetools.orm;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ru.johnlife.lifetools.data.AbstractData;
import ru.johnlife.lifetools.data.JsonOrmData;

/**
 * Created by Yan Yurkin
 * 03 June 2016
 */
public class OrmVisualizer {
    public static String getJson(Context context) {
        try {
            List<Class<? extends AbstractData>> classes = OrmHelper.dataClasses;
            OrmHelper db = new OrmHelper(context, classes);
            JSONObject jc = new JSONObject();
            for (Class<? extends AbstractData> c : classes) {
                JSONObject jObjects = new JSONObject();
                for (Object o : db.getAll(c)) {
                    JsonOrmData orm = (JsonOrmData) o;
                    JSONObject item = new JSONObject(orm.getJson());
                    jObjects.put(orm instanceof Captioned ? ((Captioned) orm).getCaption() : orm.toString(), item);
                }
                jc.put(c.getSimpleName(), jObjects);
            }
            return jc.toString();
        } catch (JSONException e) {
            Log.e("OrmVisualizer", "Exception while generating Json", e);
            return "{'exception':'"+e.getMessage()+"'}";
        }
    }
}
