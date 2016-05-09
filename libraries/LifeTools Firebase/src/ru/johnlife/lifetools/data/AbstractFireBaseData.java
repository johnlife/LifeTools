package ru.johnlife.lifetools.data;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.johnlife.lifetools.adapter.FireBaseAdapter;
import ru.johnlife.lifetools.service.BaseFirebaseBackgroundService;
import ru.johnlife.lifetools.service.OnObjectReadyListener;

/**
 * Created by yanyu on 3/29/2016.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class AbstractFireBaseData extends AbstractData {
    protected class ListObjectReadyListener<T extends AbstractFireBaseData> implements OnObjectReadyListener<T> {
        private List<T> list;

        public ListObjectReadyListener(List<T> list) {
            this.list = list;
        }

        @Override
        public void objectReady(T object) {
            list.add(object);
            notifyChanged();
        }
    }


    private String id;
    @JsonIgnore
    private List<FireBaseAdapter.BaseFirebaseListChildListener> listeners = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemPath() {
        return null == getId() ? getItemsRoot() : Path.append(getItemsRoot(), getId());
    }

    public abstract String getItemsRoot();


    protected static Map<String, String> toIds(List<? extends AbstractFireBaseData> list) {
        Map<String, String> value = new HashMap<>();
        for (AbstractFireBaseData item : list) {
            String id = item.getId();
            value.put(id, id);
        }
        return value;
    }

    protected static  <T extends AbstractFireBaseData> void fromIds(Map<String, String> ids, T template, OnObjectReadyListener<T> listener) {
        for (String id : ids.keySet()) {
            template.setId(id);
            AbstractFireBaseData.getById(template, listener);
        }
    }

    public static final <T extends AbstractFireBaseData> void getById(T template, OnObjectReadyListener<T> listener) {
        BaseFirebaseBackgroundService service = BaseFirebaseBackgroundService.getInstance();
        if (null == service) return;
        service.get(template,listener);
    }

    @JsonAnySetter
    protected void loggerSetter(String name, Object value) {
        Log.w(getClass().getSimpleName(), "Property "+name+" with value "+value+" isn't mapped");
    }
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        return getClass().equals(o.getClass()) && getId().equals(((AbstractFireBaseData)o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() ^ getId().hashCode();
    }

    public void addListener(FireBaseAdapter.BaseFirebaseListChildListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener(FireBaseAdapter.BaseFirebaseListChildListener listener) {
        listeners.remove(listener);
        return listeners.isEmpty();
    }

    public void notifyChanged() {
        for (FireBaseAdapter.BaseFirebaseListChildListener listener : listeners) {
            listener.onChildChanged(this);
        }
    }
}
