package ru.johnlife.lifetools.data;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.johnlife.lifetools.adapter.FirebaseAdapter;
import ru.johnlife.lifetools.service.BaseFirebaseBackgroundService;
import ru.johnlife.lifetools.service.OnObjectReadyListener;

public abstract class AbstractFirebaseData extends AbstractData {
    protected class ListObjectReadyListener<T extends AbstractFirebaseData> implements OnObjectReadyListener<T> {
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
    @Exclude
    private List<FirebaseAdapter.BaseFirebaseListChildListener> listeners = new ArrayList<>();

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


    protected static Map<String, String> toIds(List<? extends AbstractFirebaseData> list) {
        Map<String, String> value = new HashMap<>();
        for (AbstractFirebaseData item : list) {
            String id = item.getId();
            value.put(id, id);
        }
        return value;
    }

    protected static  <T extends AbstractFirebaseData> void fromIds(Map<String, String> ids, T template, OnObjectReadyListener<T> listener) {
        for (String id : ids.keySet()) {
            template.setId(id);
            AbstractFirebaseData.getById(template, listener);
        }
    }

    public static final <T extends AbstractFirebaseData> void getById(T template, OnObjectReadyListener<T> listener) {
        BaseFirebaseBackgroundService service = BaseFirebaseBackgroundService.getInstance();
        if (null == service) return;
        service.get(template,listener);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        return getClass().equals(o.getClass()) && getId().equals(((AbstractFirebaseData)o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() ^ getId().hashCode();
    }

    public void addListener(FirebaseAdapter.BaseFirebaseListChildListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener(FirebaseAdapter.BaseFirebaseListChildListener listener) {
        listeners.remove(listener);
        return listeners.isEmpty();
    }

    public void notifyChanged() {
        for (FirebaseAdapter.BaseFirebaseListChildListener listener : listeners) {
            listener.onChildChanged(this);
        }
    }
}
