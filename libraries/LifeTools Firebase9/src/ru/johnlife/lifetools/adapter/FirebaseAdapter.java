package ru.johnlife.lifetools.adapter;

import android.view.View;

import com.google.firebase.database.DataSnapshot;

import ru.johnlife.lifetools.data.AbstractFirebaseData;
import ru.johnlife.lifetools.listener.DataChangedListener;

/**
 * Created by yanyu on 4/7/2016.
 */
public abstract class FirebaseAdapter<T extends AbstractFirebaseData> extends BaseAdapter<T> {
    public class BaseFirebaseListChildListener extends DataChangedListener<T> {
        public BaseFirebaseListChildListener(Class<T> creator) {
            super(creator);
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            add(getItem(dataSnapshot));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            onChildChanged(getItem(dataSnapshot));
        }

        public void onChildChanged(T item) {
            replace(item);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            remove(getItem(dataSnapshot));
        }

    }

    public abstract class FirebaseViewHolder extends BaseAdapter.ViewHolder<T> {
        public FirebaseViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void assign(T item) {
            if (getItem() != null) {
                getItem().removeListener(listChildListener);
            }
            super.assign(item);
            getItem().addListener(listChildListener);
        }
    }

    private BaseFirebaseListChildListener listChildListener;

    public FirebaseAdapter(int itemLayoutId, Class<T> creator) {
        super(itemLayoutId);
        listChildListener = new BaseFirebaseListChildListener(creator);
    }

    public BaseFirebaseListChildListener getListener() {
        return listChildListener;
    }
}
