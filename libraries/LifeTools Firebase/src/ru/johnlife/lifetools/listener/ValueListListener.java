package ru.johnlife.lifetools.listener;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import ru.johnlife.lifetools.data.AbstractFireBaseData;

/**
 * Created by yanyu on 5/9/2016.
 */
public abstract class ValueListListener<T extends AbstractFireBaseData> implements ValueEventListener {
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
    public void onCancelled(FirebaseError firebaseError) {}
}
