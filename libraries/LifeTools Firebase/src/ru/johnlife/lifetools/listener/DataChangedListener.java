package ru.johnlife.lifetools.listener;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

import ru.johnlife.lifetools.data.AbstractFireBaseData;

/**
 * Created by yanyu on 4/8/2016.
 */
public abstract class DataChangedListener<T extends AbstractFireBaseData> implements ChildEventListener {
    private Class<T> creator;

    public DataChangedListener(Class<T> creator) {
        this.creator = creator;
    }

    protected T getItem(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(creator);
    }

    @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
    @Override public void onCancelled(FirebaseError firebaseError) {}
}
