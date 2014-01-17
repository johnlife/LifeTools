package ru.johnlife.lifetools.data;

public class NumberTools {
	public static int length(final int value) {
		int val = 1;
		int d = value;
		if (d >= 100000000){d /= 100000000; val += 8;}
		if (d >= 10000){d /= 10000; val += 4;}
		if (d >= 100){d /= 100; val += 2;}
		if (d >= 10){d /= 10; val += 1;}
		return val;
	}
}
