package ru.johnlife.lifetools.rest.task;

/**
 * Created by yanyu on 5/16/2016.
 */
public abstract class WrapperListener<T> implements OnTaskCompleteListener<T> {
    private OnTaskCompleteListener<T> listener;

    public WrapperListener(OnTaskCompleteListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void success(T value) {
        onSuccess(value);
        if (null != listener) {
            listener.success(value);
        }
    }

    protected abstract void onSuccess(T value);

    @Override
    public void error(String err) {
        if (null != listener) {
            listener.error(err);
        }
    }
}

