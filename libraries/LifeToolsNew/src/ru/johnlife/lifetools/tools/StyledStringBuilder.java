package ru.johnlife.lifetools.tools;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.SparseArray;

/**
 * Created by Yan Yurkin
 * 15 June 2016
 */
public class StyledStringBuilder {
    private static final SparseArray<TextAppearanceSpan> styles = new SparseArray<>();
    private Context context;
    private SpannableStringBuilder b = new SpannableStringBuilder();

    public StyledStringBuilder(Context context) {
        this.context = context;
    }

    public StyledStringBuilder append(@StringRes int s, @StyleRes int style) {
        return append(context.getResources().getString(s), style);
    }

    public StyledStringBuilder append(String s, @StyleRes int style) {
        if (s == null) return this;
        int start = b.length();
        b.append(s);
        int end = b.length();
        b.setSpan(getStyle(style), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return this;
    }

    private TextAppearanceSpan getStyle(int style) {
        TextAppearanceSpan value = styles.get(style);
        if (null == value) {
            value = new TextAppearanceSpan(context, style);
            styles.put(style, value);
        }
        return value;
    }

    public Spanned build() {
        return b;
    }

    public void clear() {
        b.clear();
    }
}
