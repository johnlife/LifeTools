package ru.johnlife.lifetools.task;

public interface OnTaskCompleteListener<E> {
	void success(E result);
	void error(String message);
}
