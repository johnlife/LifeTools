package ru.johnlife.lifetools.rest.task;

public interface OnTaskCompleteListener<T> {
	void success(T result);
	void error(String message);
}
