package ru.johnlife.lifetools.listener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import ru.johnlife.lifetools.data.AbstractFirebaseData;

/**
 * Created by yanyu on 5/9/2016.
 */
public abstract class ValueListListener<T extends AbstractFirebaseData> implements ValueEventListener {
    private Class<T> creator;

    public ValueListListener(Class<T> creator) {
        this.creator = creator;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        for (DataSnapshot jobSnap : dataSnapshot.getChildren()) {
            onItem(jobSnap.getValue(creator));
        }
    }

    protected abstract void onItem(T value);

    @Override
    public void onCancelled(DatabaseError firebaseError) {}
}
