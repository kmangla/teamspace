package com.teamspace.android.unused;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.teamspace.android.R;
import com.teamspace.android.common.ui.SettingsPreferenceFragment;
import com.teamspace.android.interfaces.ActionBarResponderInterface;
import com.teamspace.android.utils.Utils;

public class ChatListViewFragment extends Fragment implements OnItemSelectedListener, ActionBarResponderInterface {

    private int mNewItemCount;
    private ArrayAdapter<String> mAdapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.list_view, null, false);

        ListView listView = (ListView) rootView.findViewById(R.id.list_view);

		// If user has selected any color preferences, respect that.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String senderBackgroundColorStr = sharedPref.getString(
				SettingsPreferenceFragment.KEY_PREF_CHAT_SENDER_BK_COLOR,
				SettingsPreferenceFragment.KEY_DEFAULT_CHAT_BK_COLOR);
		int senderBackgroundColor = Utils.getColor(getActivity(), senderBackgroundColorStr);
		
		String senderTextColorStr = sharedPref.getString(
				SettingsPreferenceFragment.KEY_PREF_CHAT_SENDER_TEXT_COLOR,
				SettingsPreferenceFragment.KEY_DEFAULT_CHAT_TEXT_COLOR);
		int senderTextColor = Utils.getColor(getActivity(), senderTextColorStr);
		
		String otherBackgroundColorStr = sharedPref.getString(
				SettingsPreferenceFragment.KEY_PREF_CHAT_OTHER_BK_COLOR,
				SettingsPreferenceFragment.KEY_DEFAULT_CHAT_BK_COLOR);
		int otherBackgroundColor = Utils.getColor(getActivity(), otherBackgroundColorStr);
		
		String otherTextColorStr = sharedPref.getString(
				SettingsPreferenceFragment.KEY_PREF_CHAT_OTHER_TEXT_COLOR,
				SettingsPreferenceFragment.KEY_DEFAULT_CHAT_TEXT_COLOR);
		int otherTextColor = Utils.getColor(getActivity(), otherTextColorStr);

        /* Setup the adapter */
		mAdapter = new MyListAdapter(getActivity(), senderBackgroundColor,
				senderTextColor, otherBackgroundColor, otherTextColor);
        listView.setAdapter(mAdapter);

        // Add listener for row item clicks.
        listView.setOnItemClickListener(new MyOnItemClickListener(listView, mAdapter));
        
        return rootView;
    }

    private static class ChatViewHolder {
    	public TextView name;
    	public TextView taskCount;
    	public TextView lastReply;
    	public TextView initials;
    	public TextView notification;
    	public ImageView pic;
    	
    	public ChatViewHolder(View view) {
	    	name = (TextView) view.findViewById(R.id.list_row_draganddrop_textview);            
	        taskCount = (TextView) view.findViewById(R.id.task_count);
	        lastReply = (TextView) view.findViewById(R.id.last_reply);
	        pic = (ImageView) view.findViewById(R.id.employee_image_pic);
	        initials = (TextView) view.findViewById(R.id.employee_initials);
	        notification = (TextView) view.findViewById(R.id.employee_unread_replies);
    	}
    }

    private static class MyListAdapter extends ArrayAdapter<String> {

        private final Context mContext;
        private int mSenderBackgroundColor;
        private int mSenderTextColor;
        private int mOtherBackgroundColor;
        private int mOtherTextColor;

		public MyListAdapter(final Context context, int senderBackgroundColor,
				int senderTextColor, int otherBackgroundColor,
				int otherTextColor) {
			super(context, 20);
			mContext = context;
			mSenderBackgroundColor = senderBackgroundColor;
			mSenderTextColor = senderTextColor;
			mOtherBackgroundColor = otherBackgroundColor;
			mOtherTextColor = otherTextColor;
            
			for (int i = 0; i < 20; i++) {
				if (i % 2 == 0) {
					add(mContext.getString(R.string.chat_manager)
							+ mContext.getString(R.string.status_question, i));
				} else {
					if (i % 3 == 0) {
						add(mContext.getString(R.string.chat_employee)
								+ mContext.getString(R.string.status_answer1));
					} else {
						add(mContext.getString(R.string.chat_employee)
								+ mContext.getString(R.string.status_answer2));
					}
				}
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

            // If there is no view to reuse, create a new one and setup its viewholder
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
                ChatViewHolder vh = new ChatViewHolder(view);
                view.setTag(vh);
            }
            
            ChatViewHolder viewHolder = (ChatViewHolder) view.getTag();
            if (viewHolder == null || !(viewHolder instanceof ChatViewHolder)) {
            	return view;
            }
            
            viewHolder.pic.setVisibility(View.GONE);
            viewHolder.initials.setVisibility(View.GONE);
            viewHolder.notification.setVisibility(View.GONE);
            viewHolder.lastReply.setVisibility(View.GONE);
            viewHolder.taskCount.setVisibility(View.GONE);

            String text = getItem(position);
            
            // Set colors based on user's preferences
			if (text.contains("Manager:")) {
				view.setBackgroundColor(mSenderBackgroundColor);
				viewHolder.name.setTextColor(mSenderTextColor);
				viewHolder.name.setGravity(Gravity.START);
			} else {
				view.setBackgroundColor(mOtherBackgroundColor);
				viewHolder.name.setTextColor(mOtherTextColor);
				viewHolder.name.setGravity(Gravity.END);
			}

            viewHolder.name.setText(text);
            
            return view;
        }
    }

    private static class MyOnItemLongClickListener implements AdapterView.OnItemLongClickListener {

        private final ListView mListView;

        MyOnItemLongClickListener(final ListView listView) {
            mListView = listView;
        }

        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            return true;
        }
    }

    private class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        private final ListView mListView;
        private final ArrayAdapter<String> mAdapter;

        public MyOnItemClickListener(ListView listView,
				ArrayAdapter<String> adapter) {
        	mListView = listView;
            mAdapter = adapter;
		}

		@Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) { 
            
        }
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
