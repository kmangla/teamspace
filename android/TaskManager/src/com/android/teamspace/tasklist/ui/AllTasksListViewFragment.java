package com.android.teamspace.tasklist.ui;


import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.teamspace.R;
import com.android.teamspace.caching.BitmapCache;
import com.android.teamspace.caching.DataManager;
import com.android.teamspace.caching.DataManagerCallback;
import com.android.teamspace.common.ui.SettingsPreferenceFragment;
import com.android.teamspace.interfaces.ActionBarResponderInterface;
import com.android.teamspace.models.MigratedEmployee;
import com.android.teamspace.models.MigratedTask;
import com.android.teamspace.models.QuickContactHelperTask;
import com.android.teamspace.models.QuickContactHelperTask.ContactBadge;
import com.android.teamspace.utils.Constants;
import com.android.teamspace.utils.Utils;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

public class AllTasksListViewFragment extends Fragment implements OnItemSelectedListener, ActionBarResponderInterface {

	private static final int ADD_TASK = 0;
	private static final int DETAIL_TASK = 1;
    private MyListAdapter mAdapter;
    private int oldItemCount;
	private Button addButton;
	SwipeListView swipelistview;
	private int currentSortPreference = 0;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.swipe_list_view, null, false);

    	swipelistview = (SwipeListView) rootView.findViewById(R.id.swipe_list_view);
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.listview_sort_header, swipelistview, false);
        swipelistview.addHeaderView(headerView);
        
        swipelistview.setSwipeListViewListener(new BaseSwipeListViewListener() {
	         @Override
	         public void onOpened(int position, boolean toRight) {
	         }
	 
	         @Override
	         public void onClosed(int position, boolean fromRight) {
	         }
	 
	         @Override
	         public void onListChanged() {
	         }
	 
	         @Override
	         public void onMove(int position, float x) {
	         }
	 
	         @Override
	         public void onStartOpen(int position, int action, boolean right) {
	         }
	 
	         @Override
	         public void onStartClose(int position, boolean right) {
	         }
	 
	         @Override
	         public void onClickFrontView(int position) {
	        	 // Close the rows.
	        	 mAdapter.closeAllRows();

	        	 MigratedTask taskSelected = mAdapter.getItem(position - 1);
				 Intent i = new Intent(getActivity(), TaskDetailActivity.class);
				 i.putExtra(Constants.TASK_ID, taskSelected.getTaskID());
				 i.putExtra(Constants.TASK_TITLE, taskSelected.getTitle());
				 i.putExtra(Constants.EMPLOYEE_NAME, taskSelected.getEmployeeName());
				 i.putExtra(Constants.EMPLOYEE_PHONE, taskSelected.getEmployeeNumber());
				 i.putExtra(Constants.HIDE_KEYBOARD, true);
				 startActivityForResult(i, DETAIL_TASK);
	         }
	 
	         @Override
	         public void onClickBackView(int position) {
	             swipelistview.closeAnimate(position);//when you touch back view it will close
	         }
	 
	         @Override
	         public void onDismiss(int[] reverseSortedPositions) {
	 
	         }
	 
	     });
        
        addButton = (Button) headerView.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addItem();
			}
		});
        
        // If user has selected any color preferences, respect that.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String backgroundColorStr = sharedPref.getString(
				SettingsPreferenceFragment.KEY_PREF_TASK_BK_COLOR,
				SettingsPreferenceFragment.KEY_DEFAULT_TASK_BK_COLOR);
		int backgroundColor = Utils.getColor(getActivity(), backgroundColorStr);
		
		String textColorStr = sharedPref.getString(
				SettingsPreferenceFragment.KEY_PREF_TASK_TEXT_COLOR,
				SettingsPreferenceFragment.KEY_DEFAULT_TASK_TEXT_COLOR);
		int textColor = Utils.getColor(getActivity(), textColorStr);

        /* Setup the adapter */
        mAdapter = new MyListAdapter(getActivity(), backgroundColor, textColor, swipelistview);
        swipelistview.setAdapter(mAdapter);
        
        // Add sort spinner as the header of the list view.
		Spinner spinner = (Spinner) headerView.findViewById(R.id.sort_spinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(getActivity(),
		        R.array.task_sort_options, R.layout.sort_spinner_item);

		// Specify the layout to use when the list of choices appears
		sortAdapter.setDropDownViewResource(R.layout.sort_spinner_drop_down_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(sortAdapter);
		spinner.setOnItemSelectedListener(this);
		
		// Set spinner color based on user's preference
//		String sortColorStr = sharedPref.getString(
//				SettingsPreferenceFragment.KEY_PREF_SORT_BK_COLOR,
//				SettingsPreferenceFragment.KEY_DEFAULT_SORT_BK_COLOR);
//		int sortColor = Utils.getColor(getActivity(), sortColorStr);
//		spinner.setBackgroundColor(sortColor);
//		headerView.setBackgroundColor(sortColor);

//		String sortTextColorStr = sharedPref.getString(
//				SettingsPreferenceFragment.KEY_PREF_SORT_TEXT_COLOR,
//				SettingsPreferenceFragment.KEY_DEFAULT_SORT_TEXT_COLOR);
//		int sortTextColor = Utils.getColor(getActivity(), sortTextColorStr);
        
        return rootView;
    }
    
    private static class TaskViewHolder {
    	public TextView name;
    	public TextView taskCount;
    	public TextView lastReply;
    	public TextView initials;
    	public TextView notification;
    	Button delete;
		Button markCompleted;
		Button sendReminder;
		public QuickContactBadge pic; 	
    	public View frontView;
    	public View backView;
    	QuickContactHelperTask populateImageTask;
    	
    	public TaskViewHolder(View view) {
	        name = (TextView) view.findViewById(R.id.list_row_draganddrop_textview);            
	        taskCount = (TextView) view.findViewById(R.id.task_count);
	        lastReply = (TextView) view.findViewById(R.id.last_reply);
	        pic = (QuickContactBadge) view.findViewById(R.id.employee_image_pic);
	        initials = (TextView) view.findViewById(R.id.employee_initials);
	        notification = (TextView) view.findViewById(R.id.employee_unread_replies);
	        frontView = (View) view.findViewById(R.id.front);
	        backView = (View) view.findViewById(R.id.back);
	        delete = (Button) view.findViewById(R.id.swipe_button1);
	        markCompleted = (Button) view.findViewById(R.id.swipe_button2);
	        sendReminder = (Button) view.findViewById(R.id.swipe_button3);
    	}
    }

    private static class MyListAdapter extends ArrayAdapter<MigratedTask> {

        private final Context mContext;
        private int mBackgroundColor;
        private int mTextColor;
        public boolean flashNewlyAddedRows;
        public long flashRequestTime;
		private SwipeListView mSwipeListView;

		public MyListAdapter(final Context context, int backgroundColor,
				int textColor, SwipeListView swipeListView) {
        	super(context, 0);
            mContext = context;
            mBackgroundColor = backgroundColor;
            mTextColor = textColor;
            mSwipeListView = swipeListView;
            
            refreshTaskList(context);
        }
        
        private void refreshTaskList(final Context context) {
			DataManager.getInstance(context).fetchTasksForUser(Utils.getSignedInUserId(),
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
			ArrayList<MigratedTask> data = (ArrayList<MigratedTask>) DataManager
					.getInstance(context).retrieveData(
							dataStoreKey);
			
			final int position = 0;
			
			// Sort the data before adding it to adapter.
			Collections.sort(data, new Comparator<MigratedTask>() {
				@Override
				public int compare(MigratedTask lhs, MigratedTask rhs) {
					return compareTasksBasedOnTaskSortArray(lhs, rhs, position);
				}
			});
			
			// Clear the old data from adapter and insert new data.
			clear();
			for (int i = 0; i < data.size(); i++) {
				MigratedTask task = data.get(i);
                add(task);	
            }
			Utils.refreshListWithoutLosingScrollPosition(mSwipeListView, this);
        }

        public static int compareTasksBasedOnTaskSortArray(MigratedTask lhs, MigratedTask rhs, int indexInSortArray) {
    		// R.array.task_sort_options corresponds to the following cases.
    		switch (indexInSortArray) {
    			case 0:
    				if (lhs.getEmployeeName() == rhs.getEmployeeName()) {
    					return 0;
    				}
    				if (lhs.getEmployeeName() == null) {
    					return -1;
    				}
    				if (rhs.getEmployeeName() == null) {
    					return 1;
    				}
    				return lhs.getEmployeeName().compareTo(rhs.getEmployeeName());
    			case 1:
    				if (lhs.getLastUpdate() < rhs.getLastUpdate()) {
    					return 1;
    				} else if (lhs.getLastUpdate() > rhs.getLastUpdate()) {
    					return -1;
    				} else {
    					return 0;
    				}
    			case 2:
    				if (lhs.getFrequency() > rhs.getFrequency()) {
    					return 1;
    				} else if (lhs.getFrequency() < rhs.getFrequency()) {
    					return -1;
    				} else {
    					return 0;
    				}
    			case 3:
    				if (lhs.getUpdateCount() < rhs.getUpdateCount()) {
    					return 1;
    				} else if (lhs.getUpdateCount() > rhs.getUpdateCount()) {
    					return -1;
    				} else {
    					return 0;
    				}
    			case 4:
    				return lhs.getTitle().compareTo(rhs.getTitle());
    			default:
    				return 0;
    		}
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
            
            final MigratedTask task = getItem(position);
            final String taskId = task.getTaskID();
            
            // If there is no view to reuse, create a new one and setup its viewholder
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.swipe_list_row, parent, false);
                TaskViewHolder vh = new TaskViewHolder(view);
                view.setTag(vh);
            }
            
            TaskViewHolder viewHolder = (TaskViewHolder) view.getTag();
            if (viewHolder == null || !(viewHolder instanceof TaskViewHolder)) {
            	return view;
            }
            
            if (viewHolder.populateImageTask != null) {
            	viewHolder.populateImageTask.cancel(true);
            	viewHolder.populateImageTask = null;
            }
            
            // If we found a bitmap in the cache for this employee (by phone number), 
            // we do not show initials. We directly show the bitmap image. Otherwise
            // we show the initials and try to load the bitmap image in background.
			Bitmap cachedPic = BitmapCache.getInstance().retrieveData(task.getEmployeeID());
			if (cachedPic != null) {
				viewHolder.pic.setVisibility(View.VISIBLE);
				viewHolder.initials.setVisibility(View.GONE);
				viewHolder.pic.setImageBitmap(cachedPic);
			} else {
				viewHolder.pic.setVisibility(View.GONE);
				viewHolder.initials.setVisibility(View.VISIBLE);
				String initials = Utils.extractInitialsFromName(task.getEmployeeName());
	            viewHolder.initials.setText(initials);
	            
				// Start a async task to load the employee pic if available.
				ContactBadge badge = new ContactBadge(mContext, viewHolder.pic,
						task.getEmployeeID(), viewHolder.initials);
				viewHolder.populateImageTask = new QuickContactHelperTask();
				viewHolder.populateImageTask.execute(badge);
			}
			
            viewHolder.notification.setVisibility(View.GONE);

            // Set colors based on user's preferences
            view.setBackgroundColor(mBackgroundColor);
            viewHolder.frontView.setBackgroundColor(mBackgroundColor);
            viewHolder.name.setTextColor(mTextColor);
            viewHolder.taskCount.setTextColor(mTextColor);
            viewHolder.lastReply.setTextColor(mTextColor);
            
            // Set the content of text views
            viewHolder.name.setText(task.getTitle());
            viewHolder.taskCount.setText(task.getUpdateCount() + " updates till date");            
            viewHolder.lastReply.setText("Last updated: " + new Date(task.getLastUpdate()));
            
			viewHolder.delete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {					
					Toast.makeText(mContext, "Delete Clicked for task " + taskId,
							Toast.LENGTH_SHORT).show();
					closeAllRows();
					deleteTask(v.getContext(), taskId, position);					
				}
			});

			viewHolder.markCompleted
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Toast.makeText(mContext, "Mark Completed Clicked for task " + taskId,
									Toast.LENGTH_SHORT).show();
							closeAllRows();
							markTaskCompleted(v.getContext(), task, position);
						}
					});

			viewHolder.sendReminder
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Toast.makeText(mContext, "Send Reminder Clicked for task " + taskId,
									Toast.LENGTH_SHORT).show();
							closeAllRows();
						}
					});

			// For newly added item, change the appearance
			if (position == 0 && flashNewlyAddedRows
					&& (System.currentTimeMillis() - flashRequestTime) < 2000) {
				view.setBackgroundColor(mContext.getResources().getColor(
						android.R.color.holo_green_light));
				viewHolder.name.setTextColor(Utils.getColor(mContext, "black"));
				viewHolder.taskCount.setTextColor(Utils.getColor(mContext,
						"black"));
				viewHolder.lastReply.setTextColor(Utils.getColor(mContext,
						"black"));
			}
                        
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
					refreshTaskList(context);
				}
			});
			
			final MigratedTask task = getItem(position);
			if (task.getTaskID().equalsIgnoreCase(taskId)) {
				this.remove(task);
			}
		}

		protected void markTaskCompleted(final Context context,
				MigratedTask task, int position) {
			DataManager dataMgr = DataManager.getInstance(context);
			task.setStatus(Constants.CLOSED);
			dataMgr.updateTask(task, new DataManagerCallback() {

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
					refreshTaskList(context);
				}
			});
			
			// Remove the completed task from the listview
			remove(task);
		}

		protected void closeAllRows() {
        	mSwipeListView.closeOpenedItems();
		}
        
		public void sortAdapterList(final int position) {
			sort(new Comparator<MigratedTask>() {
				@Override
				public int compare(MigratedTask lhs, MigratedTask rhs) {
					return compareTasksBasedOnTaskSortArray(lhs, rhs, position);
				}
			});			
			
			notifyDataSetChanged();
		}
    }

    @Override
	public void onItemSelected(AdapterView<?> parent, View view, final int position,
			long id) {
    	currentSortPreference  = position;
		mAdapter.sortAdapterList(position);			
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
	public void addItem() {
		oldItemCount = mAdapter.getCount();
		Intent i = new Intent(getActivity(), TaskAddEditActivity.class);
		startActivityForResult(i, ADD_TASK);
	}
		
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bundle extras = null;
		switch (requestCode) {
		case DETAIL_TASK:			
			if (data != null) {
				extras = data.getExtras();
			}
			if (extras != null) {
				// Update the task title which was just changed so that the UI looks
				// responsive to the user.
				String taskID = extras.getString(Constants.TASK_ID);
				String title = extras.getString(Constants.TASK_TITLE);
				String employeeId = extras.getString(Constants.EMPLOYEE_ID);				
				String name = extras.getString(Constants.EMPLOYEE_NAME);
				String number = extras.getString(Constants.EMPLOYEE_PHONE);
				MigratedTask editedTask = null;
				for (int i = 0; i < mAdapter.getCount(); i++) {
					MigratedTask tmpTask = mAdapter.getItem(i);
					if (tmpTask.getTaskID().equalsIgnoreCase(taskID)) {
						editedTask = tmpTask;
						break;
					}
				}
				if (editedTask != null) {
					editedTask.setTitle(title);
					editedTask.setEmployeeID(employeeId);
					editedTask.setEmployeeName(name);
					editedTask.setEmployeeNumber(number);
					editedTask.setLastUpdate(System.currentTimeMillis());
					mAdapter.sortAdapterList(currentSortPreference);
				}				
			}
			mAdapter.notifyDataSetChanged();
			break;
		case ADD_TASK:
			Utils.log("onActivityResult " + requestCode);			
			if (data != null) {
				extras = data.getExtras();
			}
			if (extras != null) {
				// Create a fake task row and add it so that the UI looks
				// responsive to the user.
				String title = extras.getString(Constants.TASK_TITLE);
				String employeeId = extras.getString(Constants.EMPLOYEE_ID);				
				String name = extras.getString(Constants.EMPLOYEE_NAME);
				String number = extras.getString(Constants.EMPLOYEE_PHONE);
				MigratedTask newTask = new MigratedTask();
				newTask.setTitle(title);
				newTask.setEmployeeID(employeeId);
				newTask.setEmployeeName(name);
				newTask.setEmployeeNumber(number);
				newTask.setLastUpdate(System.currentTimeMillis());
				mAdapter.add(newTask);
				mAdapter.sortAdapterList(currentSortPreference);
			}
						
			if (mAdapter.getCount() > oldItemCount) {
				mAdapter.flashNewlyAddedRows = true;
				mAdapter.flashRequestTime = System.currentTimeMillis();
			}
			
			Utils.refreshListWithoutLosingScrollPosition(swipelistview, mAdapter);
			break;
		default:
			break;
		}
	}
	
	public void refreshUI() {
		mAdapter.refreshTaskList(getActivity());
	}	

	@Override
	public void search() {
		// TODO Auto-generated method stub
		
	}
}
