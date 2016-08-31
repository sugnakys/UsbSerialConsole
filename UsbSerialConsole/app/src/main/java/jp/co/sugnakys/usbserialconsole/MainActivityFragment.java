package jp.co.sugnakys.usbserialconsole;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivityFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainActivityFragment";

    private TextView sendMsgView;
    private LinearLayout sendViewLayout;

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

        Button sendBtn = (Button) getActivity().findViewById(R.id.sendBtn);
        sendMsgView = (TextView) getActivity().findViewById(R.id.sendMsgView);
        sendViewLayout = (LinearLayout) getActivity().findViewById(R.id.sendViewLayout);

        sendBtn.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (pref.getBoolean(getString(R.string.send_view_visible_key), true)) {
            sendViewLayout.setVisibility(View.VISIBLE);
        } else {
            sendViewLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendBtn:
                Log.d(TAG, "Send button clicked");
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
}
