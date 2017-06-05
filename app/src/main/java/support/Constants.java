package support;

import android.graphics.Bitmap;
import android.support.v4.app.FragmentTransaction;

import com.vk.sdk.VKScope;

/**
 * Created by Kirill on 31.01.2017.
 */

public class Constants {
    public static final String SETTING_NAME = "SETTING";
    public static boolean isResumed = false;
    public static final String[] mScore = new String[]{
            VKScope.MESSAGES,
            VKScope.NOHTTPS,
            VKScope.PHOTOS,
            VKScope.OFFLINE,
            VKScope.FRIENDS
    };
    public static android.support.v4.app.FragmentTransaction FragmentTransaction;
    public static int ID;
}
