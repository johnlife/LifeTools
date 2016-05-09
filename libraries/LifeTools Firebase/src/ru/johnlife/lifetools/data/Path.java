package ru.johnlife.lifetools.data;

/**
 * Created by yanyu on 4/19/2016.
 */
public class Path {
    private static final char DELIM = '/';
    private StringBuilder b;

    private Path(String base) {
        b = new StringBuilder(base);
    }

    public Path append(String s) {
        b.append(DELIM).append(s);
        return this;
    }

    public String last(String s) {
        return append(s).build();
    }

    private String build() {
        return b.toString();
    }

    @Override
    public String toString() {
        return build();
    }

    public static String append(String base, String kid) {
        return new Path(base).append(kid).build();
    }

    public static Path base(String base) {
        return new Path(base);
    }
}
