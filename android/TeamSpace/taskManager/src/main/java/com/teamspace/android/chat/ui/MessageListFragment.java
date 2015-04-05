/**
 * Fragment for Creating/Updating information about an Employee
 */

package com.teamspace.android.chat.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teamspace.android.R;
import com.teamspace.android.caching.DataManager;
import com.teamspace.android.caching.DataManagerCallback;
import com.teamspace.android.caching.DatabaseCache;
import com.teamspace.android.common.ui.TaskManagerApplication;
import com.teamspace.android.models.MigratedMessage;
import com.teamspace.android.models.MigratedTask;
import com.teamspace.android.tasklist.ui.TaskAddEditActivity;
import com.teamspace.android.utils.Constants;
import com.teamspace.android.utils.Utils;

public class MessageListFragment extends Fragment {

	private MigratedTask task;
	private static final int EDIT_TASK = 1;

	Button editButton;
	Button sendButton;
	EditText message;
	TextView title;
	TextView detail;
	TextView empName;

	protected String taskId = "";
	String taskTitle = "";
	String taskEmployeeName = "";
	String taskEmployeeNumber = "";
	
	ListView listview;
	private MyListAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Since we need to fetch task data from the database which might
		// require some time
		// and must be done on background thread, we allow the caller to pass us
		// few basic
		// information like task title which we can instantly show in the UI
		// while more details about the task are fetched from the database.
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			taskId = extras.getString(Constants.TASK_ID);
			taskTitle = extras.getString(Constants.TASK_TITLE);
			taskEmployeeName = extras.getString(Constants.EMPLOYEE_NAME);
			taskEmployeeNumber = extras.getString(Constants.EMPLOYEE_PHONE);
		}

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.message_list_fragment, null,
				false);
		
		listview = (ListView) v.findViewById(R.id.list_view);
//		View headerView = LayoutInflater.from(getActivity()).inflate(
//				R.layout.task_detail_header, listview, false);
//        listview.addHeaderView(headerView);
        
        View headerView = v.findViewById(R.id.message_list_header);
        View footerView = v.findViewById(R.id.message_list_footer);

        // Setup the adapter -
		// Create a dummy task to pass to the MyListAdapter so that it can start
		// fetching the messages for this particular taskId
		if (task == null) {
			task = new MigratedTask();
			task.setTaskID(taskId);
			task.setTitle(taskTitle);
			task.setEmployeeName(taskEmployeeName);
		}                
        mAdapter = new MyListAdapter(getActivity(), listview, task);
        listview.setAdapter(mAdapter);

        // Setup header view fields
		title = (TextView) headerView.findViewById(R.id.title);
		detail = (TextView) headerView.findViewById(R.id.detail);
		empName = (TextView) headerView.findViewById(R.id.employee_name);

		Utils.setTextViewTextAndVisibility(title, taskTitle);
		Utils.setTextViewTextAndVisibility(empName, taskEmployeeName);
		
		final TaskManagerApplication applicationContext = ((TaskManagerApplication) getFragmentActivity()
				.getApplication());

		if (Utils.isStringNotEmpty(taskId)) {
			AsyncTask<String, Void, MigratedTask> asyncTask = new AsyncTask<String, Void, MigratedTask>() {
				@Override
				protected MigratedTask doInBackground(String... taskIds) {
					return DatabaseCache.getInstance(applicationContext)
							.getMigratedTaskBlockingCall(taskIds[0]);
				}

				@Override
				protected void onPostExecute(MigratedTask cachedTask) {
					task = cachedTask;
					updateHeaderForTask(task);
				}
			};
			asyncTask.execute(taskId);
		}

		editButton = (Button) headerView.findViewById(R.id.edit_button);
		editButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				Intent i = new Intent(getActivity(), TaskAddEditActivity.class);
				i.putExtra(Constants.TASK_ID, task.getTaskID());
				i.putExtra(Constants.TASK_TITLE, task.getTitle());
				i.putExtra(Constants.EMPLOYEE_NAME, task.getEmployeeName());
				i.putExtra(Constants.HIDE_KEYBOARD, true);
				startActivityForResult(i, EDIT_TASK);
			}
		});
		
		message = (EditText) footerView.findViewById(R.id.message_text);
        sendButton = (Button) footerView.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				String messageStr = message.getText().toString();
				MigratedMessage newMessage = new MigratedMessage();
				newMessage.setTaskID(taskId);
				newMessage.setTime(System.currentTimeMillis());
				newMessage.setEmployeeID(Utils.getSignedInUserId());
				newMessage.setText(messageStr);
				DataManager.getInstance(v.getContext()).createMessage(newMessage, null);
				
				// Update the UI
				mAdapter.add(newMessage);
				
				message.clearFocus();
				message.setText(Constants.EMPTY_STRING);
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(message.getWindowToken(), 0);
			}
		});

		return v;
	}

	private void updateHeaderForTask(MigratedTask newTask) {
		// Update cached items
		taskId = newTask.getTaskID();
		taskTitle = newTask.getTitle();
		taskEmployeeName = newTask.getEmployeeName();
		
		Utils.setTextViewTextAndVisibility(title, taskTitle);
		Utils.setTextViewTextAndVisibility(detail, newTask.getDescription());

		// Convert seconds to frequency (daily, weekly, monthly etc.)
		String freq = Constants.DAILY_STR;
		if (newTask.getFrequency() <= Constants.DAILY) {
			freq = Constants.DAILY_STR;
		} else if (newTask.getFrequency() <= Constants.WEEKLY) {
			freq = Constants.WEEKLY_STR;
		} else if (newTask.getFrequency() <= Constants.BIWEEKLY) {
			freq = Constants.BIWEEKLY_STR;
		} else if (newTask.getFrequency() <= Constants.MONTHLY) {
			freq = Constants.MONTHLY_STR;
		}

		String taskFreqString = taskEmployeeName + " / " + freq;
		Utils.setTextViewTextAndVisibility(empName, taskFreqString);
	}

	public FragmentActivity getFragmentActivity() {
		return this.getActivity();
	}
	
	public void refreshUI() {
		mAdapter.refreshMessageList(getActivity());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case EDIT_TASK:
			Bundle extras = null;
			if (data != null) {
				extras = data.getExtras();
			}
			if (extras != null) {
				// Update the task details which were just changed so that the
				// UI looks responsive to the user.
				String title = extras.getString(Constants.TASK_TITLE);
				String employeeId = extras.getString(Constants.EMPLOYEE_ID);
				String name = extras.getString(Constants.EMPLOYEE_NAME);
				String number = extras.getString(Constants.EMPLOYEE_PHONE);
				long freq = extras.getLong(Constants.FREQUENCY);
				String details = extras.getString(Constants.DETAILS);

				MigratedTask editedTask = new MigratedTask();
				editedTask.setTaskID(task.getTaskID());
				editedTask.setTitle(title);
				editedTask.setFrequency(freq);
				editedTask.setDescription(details);
				editedTask.setEmployeeID(employeeId);
				editedTask.setEmployeeName(name);
				editedTask.setEmployeeNumber(number);
				editedTask.setLastUpdate(System.currentTimeMillis());
				updateHeaderForTask(editedTask);
			}
			break;
		default:
			break;
		}
	}
	
	private static class MyListAdapter extends ArrayAdapter<MigratedMessage> {

        private final Context mContext;
        public boolean flashNewlyAddedRows;
        public long flashRequestTime;
		private ListView mListView;
		MigratedTask mTask;

		public MyListAdapter(final Context context, ListView listView, MigratedTask task) {
        	super(context, 0);
            mContext = context;
            mListView = listView;
            mTask = task;
            
            refreshMessageList(context);
        }
        
        private void refreshMessageList(final Context context) {
			DataManager.getInstance(context).fetchMessagesForTask(mTask.getTaskID(),
					new DataManagerCallback() {

						@Override
						public void onDataReceivedFromCache(String dataStoreKey) {
							refreshUIForData(context, dataStoreKey);
						}

						@Override
						public void onDataReceivedFromServer(String dataStoreKey) {
							refreshUIForData(context, dataStoreKey);
						}
					});
		}

        protected void refreshUIForData(Context context, String dataStoreKey) {
        	if (dataStoreKey == null) {
				return;
			}
			ArrayList<MigratedMessage> data = (ArrayList<MigratedMessage>) DataManager
					.getInstance(context).retrieveData(
							dataStoreKey);
			
			final int position = 0;
			
			// Sort the data before adding it to adapter.
			Collections.sort(data, new Comparator<MigratedMessage>() {
				@Override
				public int compare(MigratedMessage lhs, MigratedMessage rhs) {
					return compareTasksBasedOnTaskSortArray(lhs, rhs);
				}
			});
			
			// Clear the old data from adapter and insert new data.
			clear();
			for (int i = 0; i < data.size(); i++) {
				MigratedMessage task = data.get(i);
                add(task);	
            }
			Utils.refreshListWithoutLosingScrollPosition(mListView, this);
        }

		public static int compareTasksBasedOnTaskSortArray(MigratedMessage lhs,
				MigratedMessage rhs) {			
			return (lhs.getTime() > rhs.getTime()) ? 1 : -1;
		}
        
        @Override
        public long getItemId(final int position) {
            return getItem(position).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            
            final MigratedMessage message = getItem(position);
            final String taskId = message.getTaskID();
            boolean selfRow = (message.getEmployeeID().equalsIgnoreCase(Utils.getSignedInUserId()));

            // If there is no view to reuse, create a new one and setup its viewholder
            if (view == null) {
                if (selfRow) {
                    view = LayoutInflater.from(mContext).inflate(R.layout.message_list_self_row, parent, false);
                } else {
                    view = LayoutInflater.from(mContext).inflate(R.layout.message_list_others_row, parent, false);
                }
                MessageViewHolder vh = new MessageViewHolder(view);
                view.setTag(vh);
            }
            
            MessageViewHolder viewHolder = (MessageViewHolder) view.getTag();
            if (viewHolder == null || !(viewHolder instanceof MessageViewHolder)) {
            	return view;
            }
            
            // Set the content of text views
            viewHolder.message.setText(message.getText());
            viewHolder.time.setText(Utils.getDateAndTime(message.getTime()));
            
            return view;
        }
        
		protected void deleteTask(final Context context, String taskId, int position) {
			DataManager dataMgr = DataManager.getInstance(context);
			dataMgr.deleteTask(taskId, new DataManagerCallback() {

				@Override
				public void onSuccess(String response) {
				}

				@Override
				public void onFailure(String response) {
					// Notify user about the error
					Toast.makeText(
							context,
							context.getResources().getString(
									R.string.error_task_update_failed),
							Toast.LENGTH_SHORT).show();
					refreshMessageList(context);
				}
			});
			
			final MigratedMessage task = getItem(position);
			if (task.getTaskID().equalsIgnoreCase(taskId)) {
				this.remove(task);
			}
		}

		protected void markTaskCompleted(final Context context,
				MigratedMessage message, int position) {
			DataManager dataMgr = DataManager.getInstance(context);
			dataMgr.updateMessage(message, new DataManagerCallback() {

				@Override
				public void onSuccess(String response) {
				}

				@Override
				public void onFailure(String response) {
					// Notify user about the error
					Toast.makeText(
							context,
							context.getResources().getString(
									R.string.error_task_update_failed),
							Toast.LENGTH_SHORT).show();
					refreshMessageList(context);
				}
			});
			
			// Remove the completed task from the listview
			remove(message);
		}

		public void sortAdapterList(final int position) {
			sort(new Comparator<MigratedMessage>() {
				@Override
				public int compare(MigratedMessage lhs, MigratedMessage rhs) {
					return compareTasksBasedOnTaskSortArray(lhs, rhs);
				}
			});			
			
			notifyDataSetChanged();
		}
    }
	
	private static class MessageViewHolder {
    	public TextView message;
    	public TextView time;
    	public View frontView;

    	public MessageViewHolder(View view) {
    		message = (TextView) view.findViewById(R.id.message);            
	        time = (TextView) view.findViewById(R.id.time);
	        frontView = (View) view.findViewById(R.id.front);
    	}
    }
}
