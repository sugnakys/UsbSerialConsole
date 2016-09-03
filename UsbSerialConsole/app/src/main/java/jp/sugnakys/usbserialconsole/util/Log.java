package jp.sugnakys.usbserialconsole.util;

import jp.sugnakys.usbserialconsole.BuildConfig;

public class Log {

    public static void e(String tag, String msg) {
        android.util.Log.e(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(tag, msg);
        }
    }
}
