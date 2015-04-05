package com.teamspace.android.todo.ui;


import java.util.List;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.teamspace.android.R;
import com.teamspace.android.common.ui.SettingsPreferenceFragment;
import com.teamspace.android.common.ui.SwipeItemAdapter;
import com.teamspace.android.common.ui.SwipeItemRow;
import com.teamspace.android.interfaces.ActionBarResponderInterface;
import com.teamspace.android.utils.Utils;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

public class ToDoListViewFragment extends Fragment implements OnItemSelectedListener, ActionBarResponderInterface {

    private int mNewItemCount;
    private ArrayAdapter<String> mAdapter;
    private SwipeListView swipelistview;
    private SwipeItemAdapter adapter;
    private List<SwipeItemRow> itemData;
    
    public int convertDpToPixel(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

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
//	             swipelistview.openAnimate(position); //when you touch front view it will open
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
				SettingsPreferenceFragment.KEY_PREF_TODO_BK_COLOR,
				SettingsPreferenceFragment.KEY_DEFAULT_TODO_BK_COLOR);
		int backgroundColor = Utils.getColor(getActivity(), backgroundColorStr);
		
		String textColorStr = sharedPref.getString(
				SettingsPreferenceFragment.KEY_PREF_TODO_TEXT_COLOR,
				SettingsPreferenceFragment.KEY_DEFAULT_TODO_TEXT_COLOR);
		int textColor = Utils.getColor(getActivity(), textColorStr);

        /* Setup the adapter */
        mAdapter = new ToDoListAdapter(getActivity(), backgroundColor, textColor);
        swipelistview.setAdapter(mAdapter);

        // Add sort spinner as the header of the list view.
		Spinner spinner = (Spinner) headerView.findViewById(R.id.sort_spinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(getActivity(),
		        R.array.todo_sort_options, R.layout.sort_spinner_item);
		// Specify the layout to use when the list of choices appears
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
	public void addItem() {
		mAdapter.insert(getString(R.string.newly_added_item, mNewItemCount), 0);
        mAdapter.notifyDataSetChanged();
//        mListView.insert(position, getString(R.string.newly_added_item, mNewItemCount));
        mNewItemCount++;
	}

	@Override
	public void refreshUI() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void search() {
		// TODO Auto-generated method stub
		
	}
}
