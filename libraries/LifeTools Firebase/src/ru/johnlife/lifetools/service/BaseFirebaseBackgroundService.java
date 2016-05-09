package ru.johnlife.lifetools.service;

import android.util.Log;
import android.util.LruCache;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import ru.johnlife.lifetools.data.AbstractFireBaseData;
import ru.johnlife.lifetools.data.Path;
import ru.johnlife.lifetools.data.WatchedData;

public abstract class BaseFirebaseBackgroundService extends BaseBackgroundService {
	private Firebase db = null;
	private LruCache<String, AbstractFireBaseData> objectCache = new LruCache<String, AbstractFireBaseData>(90){
		@Override
		protected void entryRemoved(boolean evicted, String key, AbstractFireBaseData oldValue, AbstractFireBaseData newValue) {
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
		public void onCancelled(FirebaseError firebaseError) {
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
		db = new Firebase(getFirebaseMainUri());
		instance = this;
		onFirebaseInitialized();
	}

	protected abstract void onFirebaseInitialized();

	protected abstract String getFirebaseMainUri();

	protected Firebase getDb() {
		return db;
	}

	public <T extends AbstractFireBaseData> void persist(T data) {
		Firebase target = db.child(data.getItemPath());
		if (null == data.getId()) {
			target = target.push();
			data.setId(target.getKey());
		}
		target.setValue(data);
	}

	public <T extends AbstractFireBaseData> void addIfNotExist(final T data) {
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

	public <T extends AbstractFireBaseData> void delete(T data) {
		db.child(data.getItemPath()).removeValue();
	}

	public <T extends AbstractFireBaseData> void addToCollection(T data, final String property, final AbstractFireBaseData kid) {
		Firebase target = db.child(data.getItemPath());
		Map<String, Object> fields = new HashMap<String, Object>() {{
			put(Path.append(property,kid.getId()), kid.getId());
		}};
		target.updateChildren(fields);
	}


	public <T extends AbstractFireBaseData> void listenForChanges(T item, ObjectChangesListener<T> listener) {
		listener.setObject(item);
		db.child(item.getItemPath()).addValueEventListener(listener);
	}

	public <T extends AbstractFireBaseData> void stopListening(T item, ObjectChangesListener listener) {
		db.child(item.getItemPath()).removeEventListener(listener);
	}

	public <T extends AbstractFireBaseData> void get(final T template, final OnObjectReadyListener<T> listener) {
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
				public void onCancelled(FirebaseError firebaseError) {
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
			public void onCancelled(FirebaseError firebaseError) {
				Log.e("FirebaseService", "get() cancelled due to error: "+firebaseError.getMessage());
				listener.objectReady(null);
			}
		});
	}

	public interface BooleanListener {
		void onTrue();
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
			public void onCancelled(FirebaseError firebaseError) {
				listener.onFalse();
			}
		});
	}

	protected void cache(AbstractFireBaseData value) {
		objectCache.put(value.getItemPath(), value);
	}

	protected void evict(AbstractFireBaseData value) {
		objectCache.remove(value.getItemPath());
	}



}