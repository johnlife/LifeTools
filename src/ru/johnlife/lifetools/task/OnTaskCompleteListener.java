package ru.johnlife.lifetools.task;

public interface OnTaskCompleteListener<E> extends OnUnbreakableTaskCompleteListener<E> {
	void error(String message);
}
