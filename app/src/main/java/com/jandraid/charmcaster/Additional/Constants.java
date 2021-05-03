package com.jandraid.charmcaster.Additional;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jandraid.charmcaster.R;

public class Constants {

    public static final String STORAGE_PATH_UPLOADS = "uploads/";
    public static final String DATABASE_PATH_UPLOADS = "uploads";

    public interface ACTION {
        public static String MAIN_ACTION = "com.jandraid.mymusicplayer.action.main";
        public static String INIT_ACTION = "com.jandraid.mymusicplayer.action.init";
        public static String PREV_ACTION = "com.jandraid.mymusicplayer.action.prev";
        public static String PLAY_ACTION = "com.jandraid.mymusicplayer.action.play";
        public static String NEXT_ACTION = "com.jandraid.mymusicplayer.action.next";
        public static String STARTFOREGROUND_ACTION = "com.jandraid.mymusicplayer.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.jandraid.mymusicplayer.action.stopforeground";
    }
    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
    public static Bitmap getDefaultAlbumArt(Context context) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.iconmusic, options);
        } catch (Error ee) {
        } catch (Exception e) {
        }
        return bm;
    }

}
