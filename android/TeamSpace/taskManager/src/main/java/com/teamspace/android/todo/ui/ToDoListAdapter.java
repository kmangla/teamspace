package com.teamspace.android.todo.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamspace.android.R;
import com.teamspace.android.utils.Utils;

public class ToDoListAdapter extends ArrayAdapter<String> {

    private final Context mContext;
    private int mBackgroundColor;
    private int mTextColor;
    
    public ToDoListAdapter(final Context context, int backgroundColor, int textColor) {
    	super(context, 20);
        mContext = context;
        mBackgroundColor = backgroundColor;
        mTextColor = textColor;
        
        for (int i = 0; i < 20; i++) {
            add(mContext.getString(R.string.todo_number, i));
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
            view = LayoutInflater.from(mContext).inflate(R.layout.swipe_list_row, parent, false);
            ToDoViewHolder vh = new ToDoViewHolder(view);
            view.setTag(vh);
        }
        
        ToDoViewHolder viewHolder = (ToDoViewHolder) view.getTag();
        if (viewHolder == null || !(viewHolder instanceof ToDoViewHolder)) {
        	return view;
        }
        
        viewHolder.pic.setVisibility(View.GONE);
        viewHolder.initials.setVisibility(View.GONE);
        viewHolder.notification.setVisibility(View.GONE);
        viewHolder.lastReply.setVisibility(View.GONE);

        // Set colors based on user's preferences
//        view.setBackgroundColor(mBackgroundColor);
//        viewHolder.frontView.setBackgroundColor(mBackgroundColor);
//        viewHolder.name.setTextColor(mTextColor);
//        viewHolder.taskCount.setTextColor(mTextColor);
        
        // Set the content of text views
        viewHolder.name.setText(getItem(position) + " blah blah blah blah blah");
        viewHolder.taskCount.setText((position + 2) + " days left");
        
        // For newly added item, change the appearance
        String text = getItem(position);
        if (text != null && text.contains("newly") && position == 0) {
        	view.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_green_light));
        	viewHolder.name.setTextColor(Utils.getColor(mContext, "black"));
        	viewHolder.taskCount.setTextColor(Utils.getColor(mContext, "black"));
        	viewHolder.lastReply.setTextColor(Utils.getColor(mContext, "black"));
        }
        
        return view;
    }
    
    private static class ToDoViewHolder {
    	public TextView name;
    	public TextView taskCount;
    	public TextView lastReply;
    	public TextView initials;
    	public TextView notification;
    	public View frontView;
    	public View backView;
    	public ImageView pic;
    	
    	public ToDoViewHolder(View view) {
	    	name = (TextView) view.findViewById(R.id.list_row_draganddrop_textview);            
	        taskCount = (TextView) view.findViewById(R.id.task_count);
	        lastReply = (TextView) view.findViewById(R.id.last_reply);
	        pic = (ImageView) view.findViewById(R.id.employee_image_pic);
	        initials = (TextView) view.findViewById(R.id.employee_initials);
	        notification = (TextView) view.findViewById(R.id.employee_unread_replies);
	        frontView = (View) view.findViewById(R.id.front);
	        backView = (View) view.findViewById(R.id.back);
    	}
    }
}
