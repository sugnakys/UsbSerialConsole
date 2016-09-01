package jp.sugnakys.usbserialconsole;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import jp.sugnakys.usbserialconsole.util.Constants;

public class LogViewActivity extends BaseAppCompatActivity {

    private static final String TAG = "LogViewActivity";

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.log_view_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void onResume() {
        super.onResume();

        File logFile = (File) getIntent().getExtras().get(Constants.EXTRA_LOG_FILE);
        if (logFile != null) {
            toolbar.setTitle(logFile.getName());
            setLogText(logFile);
        }
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