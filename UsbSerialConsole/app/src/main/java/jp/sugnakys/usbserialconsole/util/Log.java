package jp.sugnakys.usbserialconsole.util;

import jp.sugnakys.usbserialconsole.BuildConfig;

public class Log {

    public static final boolean ENABLE_RECEIVED_OUTPUT = false;

    public static void e(String tag, String msg) {
        android.util.Log.e(tag, msg);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(tag, msg);
    }
}
