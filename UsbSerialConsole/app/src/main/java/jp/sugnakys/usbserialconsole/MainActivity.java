package jp.sugnakys.usbserialconsole;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.sugnakys.usbserialconsole.settings.SettingsActivity;
import jp.sugnakys.usbserialconsole.util.Constants;
import jp.sugnakys.usbserialconsole.util.Log;
import jp.sugnakys.usbserialconsole.util.Util;

public class MainActivity extends BaseAppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String RECEIVED_TEXT_VIEW_STR = "RECEIVED_TEXT_VIEW_STR";
    private UsbService usbService;
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
    private Menu mOptionMenu;
    private MyHandler mHandler;
    private TextView receivedMsgView;
    private ScrollView scrollView;
    private boolean showTimeStamp;
    private String timestampFormat;
    private boolean autoScroll;
    private String lineFeedCode;
    private String tmpReceivedData = "";
    private boolean isUSBReady = false;
    private boolean isConnect = false;
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED:
                    Toast.makeText(context, getString(R.string.usb_permission_granted), Toast.LENGTH_SHORT).show();
                    isUSBReady = true;
                    updateOptionsMenu();
                    requestConnection();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED:
                    Toast.makeText(context, getString(R.string.usb_permission_not_granted), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB:
                    Toast.makeText(context, getString(R.string.no_usb), Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED:
                    Toast.makeText(context, getString(R.string.usb_disconnected), Toast.LENGTH_SHORT).show();
                    isUSBReady = false;
                    toggleShowLog();
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

    private void requestConnection() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setMessage(getString(R.string.confirm_connect));
        alertDialog.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                toggleShowLog();
            }
        });
        alertDialog.setNegativeButton(getString(android.R.string.cancel), null);
        alertDialog.create().show();
    }

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
        autoScroll = pref.getBoolean(getString(R.string.auto_scroll_key), true);
        lineFeedCode = Util.getLineFeedCd(
                pref.getString(getString(R.string.line_feed_code_key),
                        getString(R.string.line_feed_code_cr_lf_value)), this);

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
        mOptionMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void updateOptionsMenu() {
        if (mOptionMenu != null) {
            onPrepareOptionsMenu(mOptionMenu);
        }
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
            Toast.makeText(this, getString(R.string.action_save_log)
                    + " : " + fileName, Toast.LENGTH_SHORT).show();
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
        Pattern pattern = Pattern.compile("\n$");
        Matcher matcher = pattern.matcher(msg);
        String strResult = matcher.replaceAll("") + lineFeedCode;
        try {
            usbService.write(strResult.getBytes(Constants.CHARSET));
            Log.d(TAG, "SendMessage: " + msg);
            addReceivedData(msg);
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
        updateOptionsMenu();
    }

    private void addReceivedData(String data) {
        if (showTimeStamp) {
            addReceivedDataWithTime(data);
        } else {
            addTextView(data);
        }
    }

    private void addTextView(String data) {
        receivedMsgView.append(data);
        if (autoScroll) {
            scrollView.scrollTo(0, receivedMsgView.getBottom());
        }
    }

    private void addReceivedDataWithTime(String data) {
        String timeStamp = "[" + Util.getCurrentTime(timestampFormat) + "] ";

        tmpReceivedData += data;
        String separateStr = getLineSeparater(tmpReceivedData);
        if (!separateStr.isEmpty()) {
            String[] strArray = tmpReceivedData.split(separateStr);
            tmpReceivedData = "";
            for (int i = 0; i < strArray.length; i++) {
                if (strArray.length != 1
                        && i == (strArray.length - 1)
                        && !strArray[i].isEmpty()) {
                    tmpReceivedData = strArray[i];
                } else {
                    addTextView(timeStamp + strArray[i] + System.lineSeparator());
                }
            }
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
}
