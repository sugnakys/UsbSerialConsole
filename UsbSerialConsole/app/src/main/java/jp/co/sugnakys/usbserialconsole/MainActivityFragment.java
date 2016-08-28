package jp.co.sugnakys.usbserialconsole;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivityFragment";

    private TextView receivedMsgView;
    private TextView sendMsgView;
    private Button connectBtn;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        connectBtn = (Button) getActivity().findViewById(R.id.connectBtn);
        Button saveBtn = (Button) getActivity().findViewById(R.id.saveBtn);
        Button sendBtn = (Button) getActivity().findViewById(R.id.sendBtn);
        receivedMsgView = (TextView) getActivity().findViewById(R.id.receivedMsgView);
        sendMsgView = (TextView) getActivity().findViewById(R.id.sendMsgView);

        connectBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);

        if (!isExternalStorageWritable()) {
            saveBtn.setEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.connectBtn:
                if (((MainActivity) getActivity()).toggleShowLog()) {
                    connectBtn.setText(getResources().getString(R.string.disconnect));
                } else {
                    connectBtn.setText(getResources().getString(R.string.connect));
                }
                break;
            case R.id.saveBtn:
                ((MainActivity) getActivity()).writeToFile(receivedMsgView.getText().toString());
                break;
            case R.id.sendBtn:
                String message = sendMsgView.getText().toString();
                if (!message.isEmpty()) {
                    message += System.lineSeparator();
                    ((MainActivity) getActivity()).sendMessage(message);
                    ((MainActivity) getActivity()).addReceivedData(message);
                    sendMsgView.setText("");
                }
            default:
                Log.e(TAG, "Unknown view");
                break;
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
