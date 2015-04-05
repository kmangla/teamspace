package com.teamspace.android.common.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.teamspace.android.R;
import com.teamspace.android.utils.Utils;

public class DebugFragment extends Fragment {
	private static View mRootView;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mRootView == null) {
			mRootView = inflater.inflate(R.layout.debug, container, false);
		}
		ViewGroup parent = (ViewGroup) mRootView.getParent();
		if (parent != null) {
			parent.removeView(mRootView);
		}

        getActivity().setTitle(R.string.action_debug);

        final EditText serverIP = (EditText) mRootView.findViewById(R.id.server_ip);
        final EditText serverPort = (EditText) mRootView.findViewById(R.id.server_port);

        Button save = (Button) mRootView.findViewById(R.id.save_button);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.saveServerIP(serverIP.getText().toString());
                Utils.saveServerPort(serverPort.getText().toString());
                Toast.makeText(getActivity(), "Server address and port saved",
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });

		return mRootView;
	}
}
