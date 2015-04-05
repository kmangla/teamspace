package com.ts.messagespace;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainFragment extends Fragment {

    EditText message;
    EditText number;
    EditText server;
    EditText port;
    TextView regId;

    String regIdText;

    static final String TAG = "MessageSpace";

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment, container, false);
        message = (EditText) rootView.findViewById(R.id.message);
        number = (EditText) rootView.findViewById(R.id.number);
        server = (EditText) rootView.findViewById(R.id.server_edit_text);
        port = (EditText) rootView.findViewById(R.id.port_edit_text);

        Button saveServer = (Button) rootView.findViewById(R.id.save_server);
        saveServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.saveServer(view.getContext(), server.getText().toString());
                Utils.savePort(view.getContext(), port.getText().toString());
                Toast.makeText(getActivity(), "Server address and port saved",
                        Toast.LENGTH_SHORT).show();
            }
        });

        Button send = (Button) rootView.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.sendSMS(number.getText().toString(), message.getText().toString());
                Toast.makeText(getActivity(), "SMS sent",
                        Toast.LENGTH_SHORT).show();
            }
        });

        Button sendToServer = (Button) rootView.findViewById(R.id.send_to_server);
        sendToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.sendToServer(view.getContext(), number.getText().toString(),
                        message.getText().toString());
                Toast.makeText(getActivity(), "Sent to server",
                        Toast.LENGTH_SHORT).show();
            }
        });

        regId = (TextView) rootView.findViewById(R.id.regIDText);
        if (regIdText != null) {
            regId.setText(regIdText);
        }

        Button copy = (Button) rootView.findViewById(R.id.copy);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Gets a handle to the clipboard service.
                ClipboardManager clipboard = (ClipboardManager)
                        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

                // Creates a new text clip to put on the clipboard
                ClipData clip = ClipData.newPlainText("simple text", regId.getText());
                // Set the clipboard's primary clip.
                clipboard.setPrimaryClip(clip);
            }
        });

        return rootView;
    }

    public void setGCMRegistrationId(String id) {
        if (id != null) {
            regIdText = id;
        }

        if (regId != null && id != null) {
            regId.setText(id);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(message.getWindowToken(), 0);
    }
}
