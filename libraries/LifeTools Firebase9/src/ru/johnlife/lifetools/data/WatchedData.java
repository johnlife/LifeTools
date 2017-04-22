package ru.johnlife.lifetools.data;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanyu on 4/8/2016.
 */
public abstract class WatchedData extends AbstractFirebaseData {
    public interface Watcher<T extends AbstractFirebaseData> {
        void replacedBy(T newVersion);
    }

    @Exclude
    private List<Watcher> watchers = new ArrayList<>();

    public void watchedBy(Watcher data) {
        int idx = watchers.indexOf(data);
        if (-1 == idx) {
            watchers.add(data);
        } else {
            watchers.set(idx, data);
        }
    }

    public void unwatchedBy(Watcher watcher) {
        watchers.remove(watcher);
    }

    public void copyWatchers(WatchedData previosVersion) {
        if (!this.equals(previosVersion)) return;
        watchers = previosVersion.watchers;
        for (Watcher watcher : watchers) {
            watcher.replacedBy(this);
        }
    }
}
