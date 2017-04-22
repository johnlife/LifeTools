package ru.johnlife.lifetools.listener;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import ru.johnlife.lifetools.data.AbstractFirebaseData;

/**
 * Created by yanyu on 4/8/2016.
 */
public abstract class DataChangedListener<T extends AbstractFirebaseData> implements ChildEventListener {
    private Class<T> creator;

    public DataChangedListener(Class<T> creator) {
        this.creator = creator;
    }

    protected T getItem(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(creator);
    }

    @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
    @Override public void onCancelled(DatabaseError firebaseError) {}
}
