package ru.johnlife.lifetools.service;

/**
 * Created by yanyu on 4/7/2016.
 */
public interface OnObjectReadyListener<T extends Object> {
    void objectReady(T object);
}
