package jp.sugnakys.usbserialconsole.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {
    private static final String TAG = "Util";

    public static String getCurrentTime(String format) {
        return new SimpleDateFormat(format, Locale.US).format(new Date(System.currentTimeMillis()));
    }

    public static String getCurrentDateForFile() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date(System.currentTimeMillis()));
    }

    public static File getLogDir(Context context) {
        File file = new File(context.getExternalFilesDir(null), Constants.LOG_DIR_NAME);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(TAG, "Error: Cannot create Log directory");
            } else {
                Log.d(TAG, "Create Log directory");
            }
        }
        return file;
    }
}
