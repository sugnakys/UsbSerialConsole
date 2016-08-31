package jp.co.sugnakys.usbserialconsole;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import jp.co.sugnakys.usbserialconsole.util.Constants;
import jp.co.sugnakys.usbserialconsole.util.Log;
import jp.co.sugnakys.usbserialconsole.util.Util;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private UsbService usbService;
    private MyHandler mHandler;

    private TextView receivedMsgView;
    private ScrollView scrollView;

    private boolean showTimeStamp = true;
    private String timestampFormat;

    private String tmpReceivedData = "";

    private static final String RECEIVED_TEXT_VIEW_STR = "RECEIVED_TEXT_VIEW_STR";

    private boolean isUSBReady = false;
    private boolean isConnect = false;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED:
                    Toast.makeText(context, getString(R.string.usb_permission_granted), Toast.LENGTH_SHORT).show();
                    isUSBReady = true;
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED:
                    Toast.makeText(context, getString(R.string.usb_permission_not_granted), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB:
                    Toast.makeText(context, getString(R.string.no_usb), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED:
                    Toast.makeText(context, getString(R.string.usb_disconnected), Toast.LENGTH_SHORT).show();
                    toggleShowLog();
                    isUSBReady = false;
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED:
                    Toast.makeText(context, getString(R.string.usb_not_supported), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.e(TAG, "Unknown action");
                    break;
            }
        }
    };

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new MyHandler(this);

        setContentView(R.layout.activity_main);
        receivedMsgView = (TextView) findViewById(R.id.receivedMsgView);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(RECEIVED_TEXT_VIEW_STR, receivedMsgView.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        receivedMsgView.setText(savedInstanceState.getString(RECEIVED_TEXT_VIEW_STR));
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        showTimeStamp = pref.getBoolean(getResources().getString(R.string.timestamp_visible_key), true);
        timestampFormat = pref.getString(getString(R.string.timestamp_format_key), getString(R.string.timestamp_format_default));

        setFilters();
        startService(UsbService.class, usbConnection);
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_connect);
        item.setEnabled(isUSBReady);
        if (isConnect) {
            item.setTitle(getString(R.string.action_disconnect));
        } else {
            item.setTitle(getString(R.string.action_connect));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_connect:
                android.util.Log.d(TAG, "Connect clicked");
                toggleShowLog();
                break;
            case R.id.action_clear_log:
                Log.d(TAG, "Clear log clicked");
                receivedMsgView.setText("");
                break;
            case R.id.action_save_log:
                Log.d(TAG, "Save log clicked");
                writeToFile(receivedMsgView.getText().toString());
                break;
            case R.id.action_settings:
                Log.d(TAG, "Settings clicked");
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_log_list:
                Log.d(TAG, "Log list clicked");
                intent = new Intent(this, LogListViewActivity.class);
                startActivity(intent);
                break;
            default:
                Log.e(TAG, "Unknown id");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void writeToFile(String data) {
        String fileName = Util.getCurrentDateForFile() + Constants.LOG_EXT;
        File dirName = Util.getLogDir(getApplicationContext());

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(dirName, fileName));
            fos.write(data.getBytes(Constants.CHARSET));
            Log.d(TAG, "Save: " + fileName);
            Toast.makeText(this, "Save: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    public void sendMessage(String msg) {
        try {
            usbService.write(msg.getBytes(Constants.CHARSET));
            Log.d(TAG, "SendMessage: " + msg);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void toggleShowLog() {
        if (isConnect) {
            usbService.setHandler(null);
            isConnect = false;
        } else {
            usbService.setHandler(mHandler);
            isConnect = true;
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    if (data != null) {
                        mActivity.get().addReceivedData(data);
                    }
                    break;
                case UsbService.CTS_CHANGE:
                    Log.d(TAG, "CTS_CHANGE");
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Log.d(TAG, "DSR_CHANGE");
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Log.e(TAG, "Unknown message");
                    break;
            }
        }
    }

    public void addReceivedData(String data) {
        String timeStamp = "";
        if (showTimeStamp) {
            timeStamp = "[" + Util.getCurrentTime(timestampFormat) + "] ";
        }

        tmpReceivedData += data;

        String separateStr = getLineSeparater(tmpReceivedData);
        if (!separateStr.isEmpty()) {
            String[] strArray = tmpReceivedData.split(separateStr);
            for (String str : strArray) {
                if (str.isEmpty()) {
                    continue;
                }
                receivedMsgView.append(timeStamp + str + System.lineSeparator());
                if (Log.ENABLE_RECEIVED_OUTPUT) {
                    Log.i(TAG, "Show message: " + tmpReceivedData);
                }
            }
            tmpReceivedData = "";
            scrollView.scrollTo(0, receivedMsgView.getBottom());
        }
    }

    private String getLineSeparater(String str) {
        if (str.contains(Constants.CR_LF)) {
            return Constants.CR_LF;
        } else if (str.contains(Constants.LF)) {
            return Constants.LF;
        } else if (str.contains(Constants.CR)) {
            return Constants.CR;
        } else {
            return "";
        }
    }
}
