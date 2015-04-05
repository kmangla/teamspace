package com.teamspace.android.unused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.teamspace.android.R;
import com.teamspace.android.common.ui.TaskManagerApplication;
import com.teamspace.android.models.Employee;
import com.teamspace.android.models.Reply;
import com.teamspace.android.models.Task;
import com.teamspace.android.models.TaskMessage;
import com.teamspace.android.models.TaskUpdate;

public class TaskUpdateRepliesFragment extends ListFragment {

	private ArrayList<TaskMessage> messages;

	private class ReplyListAdapter extends ArrayAdapter<TaskMessage> {
		public ReplyListAdapter(ArrayList<TaskMessage> messages) {
			super(getActivity(), android.R.layout.simple_list_item_1, messages);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (null == convertView) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.reply_row, null);
			}
			TaskMessage r = getItem(position);
			TextView replyText = (TextView) convertView
					.findViewById(R.id.reply_text);
			replyText.setText(r.getText());
			TextView replyEmployeeName = (TextView) convertView
					.findViewById(R.id.reply_employee);
			Employee e = ((TaskManagerApplication) getActivity()
					.getApplication()).getEmployee(r.getEmployeeID());
			replyEmployeeName.setText(e.getName());
			TextView replyTime = (TextView) convertView
					.findViewById(R.id.reply_time);
			replyTime.setText(getReplyTime(r));
			return convertView;
		}

		private String getReplyTime(TaskMessage r) {
			return (String) DateFormat.format("hh:mmaa dd/MM",
					r.getTime() * 1000L);
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getActivity().getIntent().getExtras();
		long value = 0;
		if (extras != null) {
			value = extras.getLong("TASK_ID");
		}
		messages = new ArrayList<TaskMessage>();
		ArrayList<Reply> replies = ((TaskManagerApplication) getActivity()
				.getApplication()).getRepliesForTask(value);
		Iterator<Reply> itReply = replies.iterator();
		while (itReply.hasNext()) {
			TaskMessage r = itReply.next();
			messages.add(r);
		}

		Collections.sort(messages, Collections.reverseOrder());
		ReplyListAdapter adapter = new ReplyListAdapter(messages);
		setListAdapter(adapter);
		TaskManagerApplication applicationContext = ((TaskManagerApplication) getActivity()
				.getApplication());
		Task task = applicationContext.getTaskByID(value);
		long seconds = (System.currentTimeMillis() / 1000L);
		task.setLastSeenTime(seconds);
		TaskUpdate.getInstance(getActivity()).updateTask(task);
	}
}
