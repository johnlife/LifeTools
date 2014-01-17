package ru.johnlife.lifetools.task;

public interface OnTaskCompleteListener<E> extends OnCompleteListener<E> {
	void error(String message);
}
