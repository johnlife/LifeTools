package ru.johnlife.lifetools.service;

import android.util.Log;
import android.util.LruCache;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import ru.johnlife.lifetools.data.AbstractFirebaseData;
import ru.johnlife.lifetools.data.Path;
import ru.johnlife.lifetools.data.WatchedData;

public abstract class BaseFirebaseBackgroundService extends BaseBackgroundService {
	private DatabaseReference db = null;
	private LruCache<String, AbstractFirebaseData> objectCache = new LruCache<String, AbstractFirebaseData>(90){
		@Override
		protected void entryRemoved(boolean evicted, String key, AbstractFirebaseData oldValue, AbstractFirebaseData newValue) {
			if (null != newValue && (newValue instanceof WatchedData)) {
				((WatchedData) newValue).copyWatchers((WatchedData) oldValue);
			}
		}
	};

	public static abstract class ObjectChangesListener<T> implements ValueEventListener {
		private T object;

		private void setObject(T object) {
			this.object = object;
		}

		@Override
		public void onDataChange(DataSnapshot dataSnapshot) {
			object = (T) dataSnapshot.getValue(object.getClass());
			onObjectChanged(object);
		}

		public abstract void onObjectChanged(T object);

		@Override
		public void onCancelled(DatabaseError firebaseError) {
			Log.d("ObjectChangesListener", "firebaseError:" + firebaseError);
		}
	}

	private static BaseFirebaseBackgroundService instance;

	public static BaseFirebaseBackgroundService getInstance() {
		return instance;
	}

	public BaseFirebaseBackgroundService() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		db = FirebaseDatabase.getInstance().getReference();
		instance = this;
		onFirebaseInitialized();
	}

	protected abstract void onFirebaseInitialized();

	protected DatabaseReference getDb() {
		return db;
	}

	public <T extends AbstractFirebaseData> void persist(T data) {
		DatabaseReference target = db.child(data.getItemPath());
		if (null == data.getId()) {
			target = target.push();
			data.setId(target.getKey());
		}
		target.setValue(data);
	}

	public <T extends AbstractFirebaseData> void addIfNotExist(final T data) {
		isExist(data.getItemPath(), new BooleanListener() {
			@Override
			public void onTrue() {
				//do nothing
			}

			@Override
			public void onFalse() {
				persist(data);
			}
		});
	}

	public <T extends AbstractFirebaseData> void delete(T data) {
		db.child(data.getItemPath()).removeValue();
	}

	public <T extends AbstractFirebaseData> void addToCollection(T data, final String property, final AbstractFirebaseData kid) {
		DatabaseReference target = db.child(data.getItemPath());
		Map<String, Object> fields = new HashMap<String, Object>() {{
			put(Path.append(property,kid.getId()), kid.getId());
		}};
		target.updateChildren(fields);
	}


	public <T extends AbstractFirebaseData> void listenForChanges(T item, ObjectChangesListener<T> listener) {
		listener.setObject(item);
		db.child(item.getItemPath()).addValueEventListener(listener);
	}

	public <T extends AbstractFirebaseData> void stopListening(T item, ObjectChangesListener listener) {
		db.child(item.getItemPath()).removeEventListener(listener);
	}

	public <T extends AbstractFirebaseData> void get(final T template, final OnObjectReadyListener<T> listener) {
		String path = template.getItemPath();
		T value = (T) objectCache.get(path);
		if (null != value) {
			listener.objectReady(value);
		} else {
			db.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					if ((null == dataSnapshot) || (null == dataSnapshot.getValue())) {
						listener.objectReady(null);
					} else {
						T value = (T) dataSnapshot.getValue(template.getClass());
						if (null != value) {
							cache(value);
						}
						listener.objectReady(value);
					}
				}

				@Override
				public void onCancelled(DatabaseError firebaseError) {
					Log.e("FirebaseService", "get() cancelled due to error: "+firebaseError.getMessage());
					listener.objectReady(null);
				}
			});
		}
	}

	public <T extends Object> void get(final String path, final Class<T> objectClass, final OnObjectReadyListener<T> listener) {
		db.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				if ((null == dataSnapshot) || (null == dataSnapshot.getValue())) {
					listener.objectReady(null);
				} else {
					T value = (T) dataSnapshot.getValue(objectClass);
					listener.objectReady(value);
				}
			}

			@Override
			public void onCancelled(DatabaseError firebaseError) {
				Log.e("FirebaseService", "get() cancelled due to error: "+firebaseError.getMessage());
				listener.objectReady(null);
			}
		});
	}

	public interface BooleanListener {
		void onTrue();
		void onFalse();
	}

	public interface BooleanObjectListener<T extends AbstractFirebaseData> {
		void onTrue(T object);
		void onFalse();
	}

	public void isExist(String path, final BooleanListener listener) {
		db.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				if ((null == dataSnapshot) || (null == dataSnapshot.getValue())) {
					listener.onFalse();
				} else {
					listener.onTrue();
				}
			}

			@Override
			public void onCancelled(DatabaseError firebaseError) {
				listener.onFalse();
			}
		});
	}

	public <T extends AbstractFirebaseData> void isExist(final T object, final BooleanObjectListener<T> listener) {
		db.child(object.getItemPath()).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				if ((null == dataSnapshot) || (null == dataSnapshot.getValue())) {
					listener.onFalse();
				} else {
					//noinspection unchecked
					listener.onTrue((T) dataSnapshot.getValue(object.getClass()));
				}
			}

			@Override
			public void onCancelled(DatabaseError firebaseError) {
				listener.onFalse();
			}
		});
	}



	protected void cache(AbstractFirebaseData value) {
		objectCache.put(value.getItemPath(), value);
	}

	protected void evict(AbstractFirebaseData value) {
		objectCache.remove(value.getItemPath());
	}



}