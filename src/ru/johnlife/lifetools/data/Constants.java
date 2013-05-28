package ru.johnlife.lifetools.data;

import java.util.Locale;
import java.util.regex.Pattern;

public interface Constants {
	static final Pattern CURLY_SPLITTER = Pattern.compile("(?<=[}])");
	static final Pattern ANGLE_SPLITTER = Pattern.compile("(?<=[>])");
	static final String UTF_8 = "UTF-8";
	static final Locale RUSSIAN = new Locale("ru");
	static final Locale ENGLISH_US = Locale.US;
}
