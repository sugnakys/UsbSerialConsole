package jp.co.sugnakys.usbserialconsole;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class LogViewActivity extends Activity {

    private static final String TAG = "LogViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_view_main);
    }

    public void onResume() {
        super.onResume();

        File logFile = (File) getIntent().getExtras().get(Constants.EXTRA_LOG_FILE);
        setLogText(logFile);
    }

    private void setLogText(File file) {
        TextView textView = (TextView) findViewById(R.id.logView);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] readBytes = new byte[fileInputStream.available()];
            if (fileInputStream.read(readBytes) != -1) {
                String readString = new String(readBytes, Charset.forName(Constants.CHARSET));
                textView.setText(readString);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}