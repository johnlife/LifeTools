package ru.johnlife.lifetools.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanyu on 4/8/2016.
 */
public abstract class WatchedData extends AbstractFireBaseData {
    public interface Watcher<T extends AbstractFireBaseData> {
        void replacedBy(T newVersion);
    }

    @JsonIgnore
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
