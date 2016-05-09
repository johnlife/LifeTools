package com.groupchat.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.groupchat.R;
import com.groupchat.data.UserData;
import com.groupchat.picasso.Circle;

import ru.johnlife.lifetools.tools.Base64Bitmap;

/**
 * Created by yanyu on 4/22/2016.
 */
public class IconLoader {
    private static Bitmap defaultAva;
    private static Circle circle = new Circle();

    public IconLoader(Context context) {
        if (null == defaultAva) {
            defaultAva = BitmapFactory.decodeResource(context.getResources(), R.drawable.avatar);
        }
    }

    public void showAvatar(UserData user, ImageView avatarView) {
        if (null == user || null == user.getAvatar()) {
            avatarView.setImageBitmap(defaultAva);
        } else {
            Bitmap decoded = Base64Bitmap.decodeBase64(user.getAvatar());
            if (null != decoded) {
                avatarView.setImageBitmap(circle.transform(decoded));
            } else {
                avatarView.setImageBitmap(defaultAva);
            }
        }
    }
}
