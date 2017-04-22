package ru.johnlife.lifetools.orm.visualizer;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.Toast;

import me.everything.plaxien.ExplainActivity;
import ru.johnlife.lifetools.orm.OrmVisualizer;

/**
 * Created by Yan Yurkin
 * 03 June 2016
 */
public class KeyHelper {
    private static long[] times = new long[3];
    private static boolean next = true;

    public static boolean onKeyDown(int keyCode, Context context) {
        if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
            if (!next) return false;
            times[0] = times[1];
            times[1] = times[2];
            times[2] = System.currentTimeMillis();
            next = false;
        } else if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode) {
            next = true;
            if (System.currentTimeMillis() - times[0] < 5000) {
                Toast.makeText(context, "Launch!", Toast.LENGTH_SHORT).show();
                launch(context);
            }
        }
        return false;
    }

    private static void launch(Context context) {
        ExplainActivity.explainJson(context, "Data Classes", OrmVisualizer.getJson(context));
    }
}