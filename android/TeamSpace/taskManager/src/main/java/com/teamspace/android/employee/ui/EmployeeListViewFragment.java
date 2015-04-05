package com.teamspace.android.employee.ui;


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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.QuickContactBadge;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.teamspace.android.R;
import com.teamspace.android.caching.BitmapCache;
import com.teamspace.android.caching.DataManager;
import com.teamspace.android.caching.DataManagerCallback;
import com.teamspace.android.common.ui.SettingsPreferenceFragment;
import com.teamspace.android.interfaces.ActionBarResponderInterface;
import com.teamspace.android.models.MigratedEmployee;
import com.teamspace.android.models.QuickContactHelperTask;
import com.teamspace.android.models.QuickContactHelperTask.ContactBadge;
import com.teamspace.android.tasklist.ui.TasksForEmployeeActivity;
import com.teamspace.android.utils.Constants;
import com.teamspace.android.utils.Utils;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

public class EmployeeListViewFragment extends Fragment implements OnItemSelectedListener, ActionBarResponderInterface {

    private static final int ADD_EMPLOYEE = 0;
	private static final int EDIT_EMPLOYEE = 1;
	private MyListAdapter mAdapter;
	private int oldItemCount;
	private ImageButton addButton;
	SwipeListView swipelistview;
	private int currentSortPreference = 0;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.employee_list_fragment, null, false);

    	swipelistview = (SwipeListView) rootView.findViewById(R.id.swipe_list_view);
        View headerView = (View) rootView.findViewById(R.id.employee_list_header);

        swipelistview.setSwipeListViewListener(new BaseSwipeListViewListener() {
	         @Override
	         public void onOpened(int position, boolean toRight) {
                 if (toRight) {
                     swipelistview.closeAnimate(position);
                 }
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
                if (!right) {
                    Utils.closeOtherRows(swipelistview, position, mAdapter.getCount());
                }

                View row = Utils.getRowFromListView(swipelistview, position);
                if (row == null) {
                    return;
                }

                EmployeeViewHolder viewHolder = (EmployeeViewHolder) row.getTag();
                if (viewHolder == null || !(viewHolder instanceof EmployeeViewHolder)) {
                    return;
                }

                if (right) {
                    viewHolder.call.setVisibility(View.INVISIBLE);
                    viewHolder.delete.setVisibility(View.INVISIBLE);
                    viewHolder.edit.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.call.setVisibility(View.VISIBLE);
                    viewHolder.delete.setVisibility(View.VISIBLE);
                    viewHolder.edit.setVisibility(View.VISIBLE);
                }
            }
	 
	         @Override
	         public void onStartClose(int position, boolean right) {
	         }
	 
	         @Override
	         public void onClickFrontView(int position) {
	        	 // Close the rows.
	        	 mAdapter.closeAllRows();
					
	        	 MigratedEmployee employeeSelected = mAdapter.getItem(position);
	        	 Intent i = new Intent(getActivity(), TasksForEmployeeActivity.class);
	        	 i.putExtra(Constants.EMPLOYEE_ID, employeeSelected.getEmployeeID());
	        	 startActivity(i);
	         }
	 
	         @Override
	         public void onClickBackView(int position) {
	             swipelistview.closeAnimate(position);//when you touch back view it will close
	         }
	 
	         @Override
	         public void onDismiss(int[] reverseSortedPositions) {
	         }
	 
	     });       
        
        addButton = (ImageButton) headerView.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {					
				addItem();
			}
		});

		// If user has selected any color preferences, respect that.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String backgroundColorStr = sharedPref.getString(
				SettingsPreferenceFragment.KEY_PREF_EMP_BK_COLOR,
				SettingsPreferenceFragment.KEY_DEFAULT_EMP_BK_COLOR);
		int backgroundColor = Utils.getColor(getActivity(), backgroundColorStr);
		
		String textColorStr = sharedPref.getString(
				SettingsPreferenceFragment.KEY_PREF_EMP_TEXT_COLOR,
				SettingsPreferenceFragment.KEY_DEFAULT_EMP_TEXT_COLOR);
		int textColor = Utils.getColor(getActivity(), textColorStr);

        /* Setup the adapter */
        mAdapter = new MyListAdapter(getActivity(), backgroundColor, textColor, swipelistview);
        swipelistview.setAdapter(mAdapter);

        // Add sort spinner as the header of the list view.
		final Spinner spinner = (Spinner) headerView.findViewById(R.id.sort_spinner);
        ImageButton sortButton = (ImageButton) headerView.findViewById(R.id.sort_button);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(getActivity(),
		        R.array.employee_sort_options, R.layout.sort_spinner_item);
				
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
    
    private static class EmployeeViewHolder {
    	public TextView name;
    	public TextView taskCount;
    	public TextView lastReply;
        public TextView notification;
    	public TextView initials;
    	public Button moreButton;
    	public QuickContactBadge pic;
    	Button delete;
		Button edit;
		Button call;
    	public View frontView;
    	public View backView;

    	QuickContactHelperTask populateImageTask;
    	
    	public EmployeeViewHolder(View view) {
	    	name = (TextView) view.findViewById(R.id.title);
	        taskCount = (TextView) view.findViewById(R.id.subtitle);
	        lastReply = (TextView) view.findViewById(R.id.details);
            notification = (TextView) view.findViewById(R.id.notification);
	        pic = (QuickContactBadge) view.findViewById(R.id.image_view);
	        initials = (TextView) view.findViewById(R.id.initials);
	        moreButton = (Button) view.findViewById(R.id.more_button);
	        frontView = (View) view.findViewById(R.id.front);
	        backView = (View) view.findViewById(R.id.back);
	        delete = (Button) view.findViewById(R.id.swipe_button1);
	        edit = (Button) view.findViewById(R.id.swipe_button2);
	        call = (Button) view.findViewById(R.id.swipe_button3);
    	}
    }

    private static class MyListAdapter extends ArrayAdapter<MigratedEmployee> {

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

            refreshEmployeeList(context);
        }
        
		private void refreshEmployeeList(final Context context) {
			DataManager.getInstance(context).fetchEmployeesForUser(Utils.getSignedInUserId(),
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
			ArrayList<MigratedEmployee> data = (ArrayList<MigratedEmployee>) DataManager
					.getInstance(context).retrieveData(
							dataStoreKey);
			
			final int position = 0;
			
			// Sort the data before adding it to adapter.
			Collections.sort(data, new Comparator<MigratedEmployee>() {
				@Override
				public int compare(MigratedEmployee lhs, MigratedEmployee rhs) {
					return compareEmployeesBasedOnEmployeeSortArray(lhs, rhs, position);
				}
			});
			
			// Clear the old data from adapter and insert new data.
			clear();
			for (int i = 0; i < data.size(); i++) {
				MigratedEmployee emp = data.get(i);
                add(emp);	
            }
			
			Utils.refreshListWithoutLosingScrollPosition(mSwipeListView, this);
        }

		protected int compareEmployeesBasedOnEmployeeSortArray(
				MigratedEmployee lhs, MigratedEmployee rhs, int index) {
	// R.array.employee_sort_options corresponds to the following cases.
			switch (index) {
				case 0:
					return lhs.getName().compareTo(rhs.getName());
				case 1:
					return lhs.getTaskCount().compareTo(rhs.getTaskCount());
				case 2:
					return lhs.getLastUpdated().compareTo(rhs.getLastUpdated());
				case 3:
					return 0;
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
            
            final MigratedEmployee employee = getItem(position);
            final String empId = employee.getEmployeeID();
            final String empNumber = employee.getPhoneWithCountryCode();
            
            // If there is no view to reuse, create a new one and setup its viewholder
            if (view == null) {
            	view = LayoutInflater.from(mContext).inflate(R.layout.swipe_list_row_with_three_buttons, parent, false);
                EmployeeViewHolder vh = new EmployeeViewHolder(view);
                view.setTag(vh);
            }
            
            EmployeeViewHolder viewHolder = (EmployeeViewHolder) view.getTag();
            if (viewHolder == null || !(viewHolder instanceof EmployeeViewHolder)) {
            	return view;
            }
            
            if (viewHolder.populateImageTask != null) {
            	viewHolder.populateImageTask.cancel(true);
            	viewHolder.populateImageTask = null;
            }

            Button moreButton = (Button) view.findViewById(R.id.more_button);
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.closeOtherRows(mSwipeListView, position, getCount());
                    mSwipeListView.openAnimate(position);
                }
            });

            viewHolder.call.setVisibility(View.VISIBLE);
            viewHolder.delete.setVisibility(View.VISIBLE);
            viewHolder.edit.setVisibility(View.VISIBLE);
            
            // If we found a bitmap in the cache for this employee (by phone number), 
            // we do not show initials. We directly show the bitmap image. Otherwise
            // we show the initials and try to load the bitmap image in background.
			Bitmap cachedPic = BitmapCache.getInstance().retrieveData(empId);
			if (cachedPic != null) {
				viewHolder.pic.setVisibility(View.VISIBLE);
				viewHolder.initials.setVisibility(View.GONE);
				viewHolder.pic.setImageBitmap(cachedPic);
			} else {
				viewHolder.pic.setVisibility(View.GONE);
				viewHolder.initials.setVisibility(View.VISIBLE);
				String initials = Utils.extractInitialsFromName(employee.getName());
	            viewHolder.initials.setText(initials);
	            
	            // Start a async task to load the employee pic if available.
				ContactBadge badge = new ContactBadge(mContext, viewHolder.pic,
						employee.getEmployeeID(), viewHolder.initials);
	            viewHolder.populateImageTask = new QuickContactHelperTask();
	            viewHolder.populateImageTask.execute(badge);
			}
			
            viewHolder.notification.setVisibility(View.GONE);

			// Set the content of text views
			viewHolder.name.setText(employee.getName());
			viewHolder.taskCount.setText(employee.getTaskCount()
					+ " " + view.getContext().getResources()
							.getString(R.string.tasks_assigned));
			viewHolder.lastReply.setText(view.getContext().getResources()
					.getString(R.string.last_updated) + " "
					+ employee.getLastUpdated());
            
            viewHolder.delete.setText(view.getContext().getResources().getString(R.string.delete));
            viewHolder.delete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(final View v) {					
					// Remove from the UI
					remove(employee);
					notifyDataSetChanged();
					
					// Remove from database and server
					DataManager.getInstance(v.getContext()).deleteEmployee(empId, new DataManagerCallback() {

						@Override
						public void onSuccess(String response) {							
						}

						@Override
						public void onFailure(String response) {
							// Notify user about the error
							Toast.makeText(
									v.getContext(),
									v.getContext().getResources()
											.getString(
													R.string.error_employee_deletion_failed),
									Toast.LENGTH_SHORT).show();
							
							// Refresh UI
							refreshEmployeeList(v.getContext());
						}
					});
					
					// Close the rows.
					closeAllRows();
				}
			});

            viewHolder.edit.setText(view.getContext().getResources().getString(R.string.edit));
			viewHolder.edit
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							closeAllRows();
							editEmployee(v.getContext(), employee);
						}
					});

			viewHolder.call.setText(view.getContext().getResources().getString(R.string.call));
			viewHolder.call
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Toast.makeText(mContext, "Calling " + empNumber,
									Toast.LENGTH_SHORT).show();
							closeAllRows();
							Utils.callPhoneNumber(v.getContext(), empNumber);
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
        
        public void editEmployee(Context context, MigratedEmployee employee) {
    		Intent i = new Intent(context, EmployeeAddEditActivity.class);
    		i.putExtra(Constants.EMPLOYEE_ID, employee.getEmployeeID());
    		i.putExtra(Constants.EMPLOYEE_NAME, employee.getName());
    		i.putExtra(Constants.EMPLOYEE_PHONE, employee.getPhoneWithCountryCode());
    		context.startActivity(i);
    	}

		protected void closeAllRows() {
			mSwipeListView.closeOpenedItems();
		}
		
		public void sortAdapterList(final int position) {
			sort(new Comparator<MigratedEmployee>() {
				@Override
				public int compare(MigratedEmployee lhs, MigratedEmployee rhs) {
					return compareEmployeesBasedOnEmployeeSortArray(lhs, rhs,
							position);
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
		Intent i = new Intent(getActivity(), EmployeeAddEditActivity.class);
		startActivityForResult(i, ADD_EMPLOYEE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case EDIT_EMPLOYEE:
			mAdapter.notifyDataSetChanged();
			break;
		case ADD_EMPLOYEE:
			Utils.log("onActivityResult " + requestCode);
			Bundle extras = null;
			if (data != null) {
				extras = data.getExtras();
			}			
			if (extras != null) {
				// Create a fake employee row and add it so that the UI looks
				// responsive to the user.
				String employeeName = extras.getString(Constants.EMPLOYEE_NAME);
				String employeePhone = extras.getString(Constants.EMPLOYEE_PHONE);
				MigratedEmployee newEmp = new MigratedEmployee();
				newEmp.setName(employeeName);
				newEmp.setPhoneWithContryCode(employeePhone);
				newEmp.setTaskCount("0");
				newEmp.setLastUpdated(Utils.getCurrentDate());
				mAdapter.add(newEmp);
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
		mAdapter.refreshEmployeeList(getActivity());
	}

	@Override
	public void search() {
		// TODO Auto-generated method stub
		
	}
}
