package com.android.teamspace.tasklist.ui;


import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.teamspace.R;
import com.android.teamspace.caching.DataManager;
import com.android.teamspace.caching.DataManagerCallback;
import com.android.teamspace.common.ui.SettingsPreferenceFragment;
import com.android.teamspace.interfaces.ActionBarResponderInterface;
import com.android.teamspace.models.MigratedTask;
import com.android.teamspace.utils.Utils;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

public class TasksForEmployeeListViewFragment extends Fragment implements OnItemSelectedListener, ActionBarResponderInterface {

    private MyListAdapter mAdapter;
    private int oldItemCount;
    private String mEmployeeId;
	private Button addTaskButton;
    
    public TasksForEmployeeListViewFragment(String employeeId) {
    	super();
    	mEmployeeId = employeeId;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.swipe_list_view, null, false);

    	final SwipeListView swipelistview = (SwipeListView) rootView.findViewById(R.id.swipe_list_view);
        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.listview_sort_header, swipelistview, false);
        swipelistview.addHeaderView(headerView);
        
        addTaskButton = (Button) headerView.findViewById(R.id.add_button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {					
				Toast.makeText(v.getContext(), "Add Button Clicked",
						Toast.LENGTH_SHORT).show();
				addItem();
			}
		});
        
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
	         }
	 
	         @Override
	         public void onClickBackView(int position) {
	             swipelistview.closeAnimate(position);//when you touch back view it will close
	         }
	 
	         @Override
	         public void onDismiss(int[] reverseSortedPositions) {
	 
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
        mAdapter = new MyListAdapter(getActivity(), backgroundColor, textColor, mEmployeeId, swipelistview);
        swipelistview.setAdapter(mAdapter);

        // Add sort spinner as the header of the list view.
		Spinner spinner = (Spinner) headerView.findViewById(R.id.sort_spinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(getActivity(),
		        R.array.task_for_emp_sort_options, R.layout.sort_spinner_item);

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
//
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
    	public ImageView pic;    	
    	public View frontView;
    	public View backView;
    	
    	public TaskViewHolder(View view) {
	    	name = (TextView) view.findViewById(R.id.list_row_draganddrop_textview);            
	        taskCount = (TextView) view.findViewById(R.id.task_count);
	        lastReply = (TextView) view.findViewById(R.id.last_reply);
	        pic = (ImageView) view.findViewById(R.id.employee_image_pic);
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
        private String mEmployeeId;

		public MyListAdapter(final Context context, int backgroundColor,
				int textColor, String employeeId, SwipeListView swipeListView) {
        	super(context, 0);
            mContext = context;
            mBackgroundColor = backgroundColor;
            mTextColor = textColor;
            mSwipeListView = swipeListView;
            mEmployeeId = employeeId;
            
            refreshTaskList(context);
        }
        
        private void refreshTaskList(final Context context) {
        	Utils.log("TFELVF mEmployeeId = " + mEmployeeId);
			DataManager.getInstance(context).fetchTasksForEmployee(mEmployeeId,
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
			// Clear the old data from adapter and insert new data.
			clear();
			for (int i = 0; i < data.size(); i++) {
				MigratedTask task = data.get(i);
                add(task);	
            }
			notifyDataSetChanged();
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
            
            viewHolder.pic.setVisibility(View.GONE);
            viewHolder.initials.setVisibility(View.GONE);
            viewHolder.notification.setVisibility(View.GONE);

            // Set colors based on user's preferences
            view.setBackgroundColor(mBackgroundColor);
            viewHolder.frontView.setBackgroundColor(mBackgroundColor);
            viewHolder.name.setTextColor(mTextColor);
            viewHolder.taskCount.setTextColor(mTextColor);
            viewHolder.lastReply.setTextColor(mTextColor);

            MigratedTask task = getItem(position);
            final String taskId = task.getTaskID();
            
            viewHolder.delete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {					
					Toast.makeText(mContext, "Delete Clicked for task " + taskId,
							Toast.LENGTH_SHORT).show();
					closeAllRows();
				}
			});

			viewHolder.markCompleted
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Toast.makeText(mContext, "Mark Completed Clicked for task " + taskId,
									Toast.LENGTH_SHORT).show();
							closeAllRows();
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
            
            // Set the content of text views
            viewHolder.name.setText(task.getTitle());
            viewHolder.taskCount.setText(task.getUpdateCount() + " updates till date");            
            viewHolder.lastReply.setText("Last updated: " + new Date(task.getLastUpdate()));
            
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
            
            // Set dummy data in imageView and notification bubble
            if (position % 2 == 0) {
            	viewHolder.notification.setText(String.valueOf(position));
            	viewHolder.notification.setVisibility(View.VISIBLE);
            	viewHolder.initials.setText("PP");
            	viewHolder.initials.setVisibility(View.VISIBLE);
            } else {            	
            	if (position % 3 == 0) {
            		viewHolder.initials.setText("VT");
            		viewHolder.initials.setVisibility(View.VISIBLE);
            	} else {
            		viewHolder.pic.setVisibility(View.VISIBLE);
            	}
            }
            
            return view;
        }
        
        protected void closeAllRows() {
        	mSwipeListView.closeOpenedItems();
		}
    }

    @Override
	public void onItemSelected(AdapterView<?> parent, View view, final int position,
			long id) {
    	Utils.log("item selected at position " + position);
		
		mAdapter.sort(new Comparator<MigratedTask>() {
			@Override
			public int compare(MigratedTask lhs, MigratedTask rhs) {
				// R.array.task_for_emp_sort_options corresponds to the following cases.
				switch (position) {
					case 0:
						if (lhs.getLastUpdate() > rhs.getLastUpdate()) {
							return 1;
						} else if (lhs.getLastUpdate() < rhs.getLastUpdate()) {
							return -1;
						} else {
							return 0;
						}						
					case 1:
						if (lhs.getFrequency() > rhs.getFrequency()) {
							return 1;
						} else if (lhs.getFrequency() < rhs.getFrequency()) {
							return -1;
						} else {
							return 0;
						}
					case 2:
						return 0;
					case 3:
						return lhs.getTitle().compareTo(rhs.getTitle());					
					default:
						return 0;
				}
			}
		});
		
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
	public void addItem() {
		oldItemCount = mAdapter.getCount();
//		Intent i = new Intent(getActivity(), EmployeeAddActivity.class);
//		startActivityForResult(i, 0);
	}
	
	public void refreshUI() {
		mAdapter.refreshTaskList(getActivity());
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mAdapter.getCount() > oldItemCount) {
			mAdapter.flashNewlyAddedRows = true;
			mAdapter.flashRequestTime = System.currentTimeMillis();
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void search() {
		// TODO Auto-generated method stub
		
	}
}
