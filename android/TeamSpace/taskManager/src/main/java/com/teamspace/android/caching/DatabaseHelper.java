package com.teamspace.android.caching;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.teamspace.android.models.Employee;
import com.teamspace.android.models.EmployeeState;
import com.teamspace.android.models.MigratedEmployee;
import com.teamspace.android.models.MigratedMessage;
import com.teamspace.android.models.MigratedTask;
import com.teamspace.android.models.Reply;
import com.teamspace.android.models.Task;
import com.teamspace.android.utils.TimeUtil;

class DatabaseHelper extends SQLiteOpenHelper {

	private static final String FILENAME = "tasks.sqlite";
	private static final int VERSION = 25;

	public DatabaseHelper(Context c) {
		super(c, FILENAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table tasks (_id integer primary key autoincrement, "
				+ "employee_id bigint, description varchar(100), completed boolean,"
				+ "last_sent_time bigint, frequency bigint, last_seen_time bigint,"
				+ "is_repeated boolean, repeat_frequency bigint, repeat_day bigint, completed_current boolean,"
				+ "halted boolean/*deprecated*/)");
		
		db.execSQL("create table employee (_id integer primary key autoincrement, "
				+ "number varchar(20), name varchar(30))");

		db.execSQL("create table employee_state (last_sent_task bigint, "
				+ "reply_received boolean, task_sent_time bigint, employee_id bigint, reminders_sent int, task_reminder_start_time bigint, owner_update_id bigint)");

		db.execSQL("create table replies (_id integer primary key autoincrement, "
				+ "reply_text varchar(255), employee_id bigint, received_time bigint, task_id bigint)");

		db.execSQL("create table task_pause (task_id integer UNIQUE, paused_till bigint)");

		
		/* Tables for Migration*/
		db.execSQL("create table new_tasks (task_id varchar(100), "
				+ "description varchar(1000), title varchar(140), status varchar(100), user_id varchar(100),"
				+ "company_id varchar(100), employee_id varchar(100), employee_name varchar(100), employee_number varchar(100), frequency bigint,"
				+ "created_on bigint, last_reminder bigint, update_count bigint, last_update bigint, last_seen bigint)");
		
		db.execSQL("create table new_employees (name varchar(100), "
				+ "phone varchar(100), company_id varchar(100), user_id varchar(100),"
				+ "employee_id varchar(100), designation varchar(256), task_count varchar(100), last_updated varchar(100))");
		
		db.execSQL("create table new_messages (" +
				"message_id varchar(100), task_id varchar(100)," +
				"text varchar(1024), employee_id varchar(100), " +
				"time bigint)"); 

		Log.i("task_db", "This was called");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int versionOld, int versionNew) {
		if (versionOld <= 18 && versionNew >= 19) {
			db.execSQL("create table owner_preference (reminder_freq integer, start_time integer, end_time integer)");
		}
		if (versionOld <= 19 && versionNew >= 20) {
			db.execSQL("alter table tasks add column is_repeated boolean");
			db.execSQL("alter table tasks add column repeat_frequency bigint");
			db.execSQL("alter table tasks add column repeat_day bigint");
			db.execSQL("alter table tasks add column completed_current boolean");
		}
		if (versionOld <= 20 && versionNew >= 21) {
			db.execSQL("alter table tasks add column halted boolean");
		}
		if (versionOld <= 21 && versionNew >= 22) {
			db.execSQL("create table new_tasks (task_id varchar(100), "
					+ "description varchar(1000), status varchar(100), user_id varchar(100),"
					+ "company_id varchar(100), employee_id varchar(100), frequency bigint)"); 
		}
		if (versionOld <= 22 && versionNew >= 23) {
			db.execSQL("alter table new_tasks add column created_on bigint");
			db.execSQL("alter table new_tasks add column last_reminder bigint");
			db.execSQL("alter table new_tasks add column update_count bigint");
			db.execSQL("alter table new_tasks add column last_update bigint");
			db.execSQL("alter table new_tasks add column last_seen bigint");			
			db.execSQL("create table new_employees (name varchar(100), "
					+ "phone varchar(100), company_id varchar(100), user_id varchar(100),"
					+ "employee_id varchar(100), designation varchar(256), task_count varchar(100), last_updated varchar(100))"); 

		}
		if (versionOld <= 23 && versionNew >= 24) {
			db.execSQL("create table new_messages (" +
					"message_id varchar(100), task_id varchar(100)," +
					"text varchar(1024), employee_id varchar(100), " +
					"time bigint)"); 
		}		
		if (versionOld <= 24 && versionNew >= 25) {
			db.execSQL("alter table new_tasks add column employee_name varchar(100)");
			db.execSQL("alter table new_tasks add column employee_number varchar(100)");
			db.execSQL("alter table new_tasks add column title varchar(140)");
		}

	}
		
	public ArrayList<Task> getTasks(String employeeID, boolean completed) {
		ArrayList<Task> fetchedTasks = new ArrayList<Task>();

		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query(
				"tasks",
				null,
				"employee_id = ? AND completed = ?",
				new String[] { employeeID,
						String.valueOf(completed) }, null, null, null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			fetchedTasks.add(getTaskFromCursor(cursor));
			cursor.moveToNext();
		}

		cursor.close();
		return fetchedTasks;
	}
	
	public ArrayList<Task> getRepeatedTasks(String employeeID) {
		ArrayList<Task> fetchedTasks = new ArrayList<Task>();

		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query(
				"tasks",
				null,
				"is_repeated = 1 AND completed = 0 AND employee_id = ?",
				new String[] { employeeID }, null, null, null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			fetchedTasks.add(getTaskFromCursor(cursor));
			cursor.moveToNext();
		}

		cursor.close();
		return fetchedTasks;
	}
	
	public ArrayList<Task> getAllRepeatedTasks() {
		ArrayList<Task> fetchedTasks = new ArrayList<Task>();

		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query(
				"tasks",
				null,
				"is_repeated = 1 AND completed = 0",
				null, null, null, null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			fetchedTasks.add(getTaskFromCursor(cursor));
			cursor.moveToNext();
		}

		cursor.close();
		return fetchedTasks;
	}

	public ArrayList<Employee> getAllEmployees() {
		ArrayList<Employee> fetchedEmployees = new ArrayList<Employee>();

		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query("employee", null, null, null, null,
				null, null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			fetchedEmployees.add(getEmployeeFromCursor(cursor));
			cursor.moveToNext();
		}

		cursor.close();
		return fetchedEmployees;
	}

	public ArrayList<EmployeeState> getAllEmployeeStates() {
		ArrayList<EmployeeState> fetchedEmployeeStates = new ArrayList<EmployeeState>();

		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query("employee_state", null, null, null, null,
				null, null);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			EmployeeState s = getEmployeeStateFromCursor(cursor);
			fetchedEmployeeStates.add(s);
			cursor.moveToNext();
		}

		cursor.close();
		return fetchedEmployeeStates;
	}

	public Task getTaskByID(long taskID) {
		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query("tasks", null, "_id = ?",
				new String[] { String.valueOf(taskID) }, null, null, null);

		cursor.moveToFirst();
		Task task = getTaskFromCursor(cursor);
		cursor.close();
		return task;
	}


	public Employee getEmployeeByPhone(String number) {
		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query("employee", null, "number = ?",
				new String[] { number }, null, null, null);
		cursor.moveToFirst();
		Employee employee = getEmployeeFromCursor(cursor);
		cursor.close();
		return employee;
	}

	public EmployeeState getEmployeeStateByEmployeeID(String employeeID) {
		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query("employee_state", null,
				"employee_id = ?", new String[] { String.valueOf(employeeID) },
				null, null, null);
		cursor.moveToFirst();
		EmployeeState employeeState = getEmployeeStateFromCursor(cursor);
		cursor.close();
		return employeeState;
	}

	
	public ArrayList<Reply> getAllReplies(int numDays) {
		long seconds = TimeUtil.currentTimeSec();
		SQLiteDatabase database = getReadableDatabase();
		ArrayList<Reply> fetchedReplies = new ArrayList<Reply>();

		Cursor cursor = database.query("replies", null, "received_time > ?",
				new String[] { String.valueOf(seconds - numDays * 86400) },
				null, null, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			fetchedReplies.add(getReplyFromCursor(cursor));
			cursor.moveToNext();
		}

		cursor.close();
		return fetchedReplies;
	}

	public ArrayList<Task> getCompletedTasks(int numDays) {
		long seconds = TimeUtil.currentTimeSec();
		SQLiteDatabase database = getReadableDatabase();
		ArrayList<Task> fetchedTasks = new ArrayList<Task>();

		Cursor cursor = database.query("tasks", null,
				"last_sent_time > ? and completed = ?",
				new String[] { String.valueOf(seconds - numDays * 86400),
						String.valueOf(1) }, null, null, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			fetchedTasks.add(getTaskFromCursor(cursor));
			cursor.moveToNext();
		}

		cursor.close();
		return fetchedTasks;
	}

	public ArrayList<Task> getIncompletedTasks() {
		SQLiteDatabase database = getReadableDatabase();
		ArrayList<Task> fetchedTasks = new ArrayList<Task>();

		Cursor cursor = database.query("tasks", null, "completed = ?",
				new String[] { String.valueOf(0) }, null, null, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			fetchedTasks.add(getTaskFromCursor(cursor));
			cursor.moveToNext();
		}

		cursor.close();
		return fetchedTasks;
	}

	public Employee getEmployee(String employeeID) {
		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query("employee", null, "_id = ?",
				new String[] { employeeID }, null, null, null);
		cursor.moveToFirst();

		Employee employee = getEmployeeFromCursor(cursor);
		cursor.close();
		return employee;
	}

	public ArrayList<Reply> getRepliesForTask(long taskID) {
		SQLiteDatabase database = getReadableDatabase();
		ArrayList<Reply> fetchedReplies = new ArrayList<Reply>();

		Cursor cursor = database.query("replies", null, "task_id = ?",
				new String[] { String.valueOf(taskID) }, null, null,
				"received_time DESC");
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			fetchedReplies.add(getReplyFromCursor(cursor));
			cursor.moveToNext();
		}

		cursor.close();
		return fetchedReplies;
	}

	public void deleteEmployee(Employee employee) {
		SQLiteDatabase database = getWritableDatabase();

		database.delete("employee", "_id = ?",
				new String[] { String.valueOf(employee.getId()) });
		database.delete("employee_state", "employee_id = ?",
				new String[] { String.valueOf(employee.getId()) });
	}

	public boolean updateEmployee(Employee employee) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("name", employee.getName());
		cv.put("number", employee.getNumber());

		long id = database.update("employee", cv, "_id = ?",
				new String[] { String.valueOf(employee.getId()) });
		return id != -1;
	}

	public boolean insertEmployee(Employee employee) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("name", employee.getName());
		cv.put("number", employee.getNumber());

		long id = database.insert("employee", null, cv);
		employee.setId(String.valueOf(id));
		if (id == -1) {
			return false;
		}
		EmployeeState employeeState = new EmployeeState();
		employeeState.setTaskSentTime(-1);
		employeeState.setLastTaskSentID(-1);
		employeeState.setReplyReceived(true);
		employeeState.setRemindersSent(0);

		return insertEmployeeState(id, employeeState);
	}

	public boolean insertTask(Task task) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("description", task.getDescription());
		cv.put("employee_id", task.getEmployeeID());
		cv.put("completed", task.isCompleted());
		cv.put("last_sent_time", task.getLastSent());
		cv.put("frequency", task.getFrequency());
		cv.put("last_seen_time", task.getLastSeenTime());
		cv.put("completed_current", task.isCompletedCurrent());
		cv.put("is_repeated", task.isRepeated());
		cv.put("repeat_day", task.getRepeatDay());
		cv.put("repeat_frequency", task.getRepeatFrequency());

		long id = database.insert("tasks", null, cv);
		task.setId(id);

		return (id != -1) && insertTaskPause(task);
	}

	public boolean updateTask(Task task) {
		SQLiteDatabase database = getWritableDatabase();

		Log.i("task", task + "");
		
		ContentValues cv = new ContentValues();

		cv.put("description", task.getDescription());
		cv.put("employee_id", task.getEmployeeID());
		cv.put("completed", task.isCompleted());
		cv.put("last_sent_time", task.getLastSent());
		cv.put("frequency", task.getFrequency());
		cv.put("last_seen_time", task.getLastSeenTime());
		cv.put("completed_current", task.isCompletedCurrent());
		cv.put("is_repeated", task.isRepeated());
		cv.put("repeat_day", task.getRepeatDay());
		cv.put("repeat_frequency", task.getRepeatFrequency());

		long id = database.update("tasks", cv, "_id = ?",
				new String[] { String.valueOf(task.getId()) });
		return id != -1;
	}

	public boolean updateEmployeeState(EmployeeState employeeState,
			String employeeID) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("task_sent_time", employeeState.getTaskSentTime());
		cv.put("last_sent_task", employeeState.getLastTaskSentID());
		cv.put("reply_received", employeeState.isReplyReceived());
		cv.put("reminders_sent", employeeState.getRemindersSent());
		cv.put("task_reminder_start_time",
				employeeState.getTaskSentReminderTime());

		long id = database.update("employee_state", cv, "employee_id = ?",
				new String[] { employeeID });
		return id != -1;
	}

	private boolean insertEmployeeState(long employeeID,
			EmployeeState employeeState) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("employee_id", employeeID);
		cv.put("task_sent_time", employeeState.getTaskSentTime());
		cv.put("last_sent_task", employeeState.getLastTaskSentID());
		cv.put("reply_received", employeeState.isReplyReceived());
		cv.put("reminders_sent", employeeState.getRemindersSent());
		cv.put("task_reminder_start_time",
				employeeState.getTaskSentReminderTime());

		long id = database.insert("employee_state", null, cv);
		return id != -1;
	}

	public boolean updateReply(Reply reply) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("reply_text", reply.getReplyText());
		cv.put("employee_id", reply.getEmployeeID());
		cv.put("received_time", reply.getReceivedTime());
		cv.put("task_id", reply.getTaskID());

		long id = database.update("replies", cv, "_id = ?",
				new String[] { String.valueOf(reply.getId()) });
		return id != -1;
	}

	public long insertReply(Reply reply) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("reply_text", reply.getReplyText());
		cv.put("employee_id", reply.getEmployeeID());
		cv.put("received_time", reply.getReceivedTime());
		cv.put("task_id", reply.getTaskID());

		long id = database.insert("replies", null, cv);
		return id;
	}

	public int getIncompleteTasksCount(Employee employee) {
		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query("tasks", null,
				"completed = 0 AND employee_id = ?",
				new String[] { String.valueOf(employee.getId()) }, null, null,
				null);
		int cnt = cursor.getCount();
		cursor.close();
		return cnt;

	}

	public int getPendingTasksCount(Employee employee) {
		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query("tasks", null,
				"completed = 0 AND employee_id = ?",
				new String[] { String.valueOf(employee.getId()) }, null, null,
				null);
		cursor.moveToFirst();
		int cnt = 0;
		while (!cursor.isAfterLast()) {
			Task task = getTaskFromCursor(cursor);
			if (task.needsToBeSent()) {
				cnt++;
			}
			cursor.moveToNext();
		}
		cursor.close();
		EmployeeState state = getEmployeeStateByEmployeeID(employee.getId());
		if (!state.isReplyReceived()) {
			cnt++;
		}
		return cnt;

	}

	public int getUnreadRepliesForEmployee(Employee employee) {
		ArrayList<Task> tasks = getTasks(employee.getId(), false);
		Iterator itTasks = tasks.iterator();

		int cnt = 0;
		while (itTasks.hasNext()) {
			Task task = (Task) itTasks.next();
			cnt += getUnreadRepliesForTask(task);
		}
		return cnt;
	}

	public int getUnreadRepliesForTask(Task task) {
		SQLiteDatabase database = getReadableDatabase();
		Cursor cursor = database.query(
				"replies",
				null,
				"received_time >  ? AND task_id = ?",
				new String[] { String.valueOf(task.getLastSeenTime()),
						String.valueOf(task.getId()) }, null, null, null);
		int cnt = cursor.getCount();
		cursor.close();
		return cnt;
	}

	public boolean insertTaskPause(Task task) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("task_id", task.getId());
		cv.put("paused_till", 0);

		long id = database.insert("task_pause", null, cv);
		return id != -1;
	}

	public boolean updateTaskPause(Task task, long pausedTill) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("task_id", task.getId());
		cv.put("paused_till", pausedTill);

		long id = database.update("task_pause", cv, "task_id = ?",
				new String[] { String.valueOf(task.getId()) });
		return id != -1;
	}

	public boolean removeTaskPause(Task task) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("task_id", task.getId());
		cv.put("paused_till", 0);

		Log.i("test", "here remove task pause");
		long id = database.update("task_pause", cv, "task_id = ?",
				new String[] { String.valueOf(task.getId()) });
		return id != -1;
	}
	
	public long getTaskPausedTill(Task task) {
		SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database
            		   .query("task_pause", null, "task_id = ?",
                              new String[] { String.valueOf(task.getId()) }, null,
                              null, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
             insertTaskPause(task);
             return 0;
        }
        long pausedTill = cursor.getLong(cursor.getColumnIndex("paused_till"));
        cursor.close();
        return pausedTill;
    }
	
	public void setMigratedMessage(MigratedMessage message) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("task_id", message.getTaskID());
		cv.put("employee_id", message.getEmployeeID());
		cv.put("text",message.getText());
		cv.put("time", message.getTime());
		cv.put("message_id", message.getMessageID());

		database.insert("new_messages", null, cv);
	}
	
	public void updateMigratedMessage(MigratedMessage message) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("task_id", message.getTaskID());
		cv.put("employee_id", message.getEmployeeID());
		cv.put("text",message.getText());
		cv.put("time", message.getTime());
		cv.put("message_id", message.getMessageID());


		database.update("new_messages", cv, "message_id = ?",
				new String[] { String.valueOf(message.getMessageID()) });
	}

	public MigratedMessage getMigratedMessage(String messageID) {
		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query("new_messages", null, "message_id = ?",
				new String[] { messageID }, null, null, null);
		cursor.moveToFirst();

		MigratedMessage migratedMessage = getMigratedMessageFromCursor(cursor);
		cursor.close();
		return migratedMessage;
	}
	
	public void deleteMigratedMessage(MigratedMessage message) {
		SQLiteDatabase database = getWritableDatabase();

		database.delete("new_messages", "message_id = ?",
			new String[] { String.valueOf(message.getMessageID()) });
	}

	
	public void setMigratedTask(MigratedTask task) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("task_id", task.getTaskID());
		cv.put("user_id", task.getUserID());
		cv.put("company_id", task.getCompanyID());
		cv.put("employee_id", task.getEmployeeID());
		cv.put("employee_name", task.getEmployeeName());
		cv.put("employee_number", task.getEmployeeNumber());
		cv.put("description", task.getDescription());
		cv.put("title", task.getTitle());
		cv.put("status", task.getStatus());
		cv.put("frequency", task.getFrequency());
		cv.put("created_on", task.getCreatedOn());
		cv.put("last_reminder", task.getLastReminder());
		cv.put("last_update", task.getLastUpdate());
		cv.put("last_seen", task.getLastSeen());
		cv.put("update_count", task.getUpdateCount());		

		database.insert("new_tasks", null, cv);
	}
	
	public void updateMigratedTask(MigratedTask task) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("task_id", task.getTaskID());
		cv.put("user_id", task.getUserID());
		cv.put("company_id", task.getCompanyID());
		cv.put("employee_id", task.getEmployeeID());
		cv.put("employee_name", task.getEmployeeName());
		cv.put("employee_number", task.getEmployeeNumber());
		cv.put("description", task.getDescription());
		cv.put("title", task.getTitle());
		cv.put("status", task.getStatus());
		cv.put("frequency", task.getFrequency());
		cv.put("created_on", task.getCreatedOn());
		cv.put("last_reminder", task.getLastReminder());
		cv.put("last_update", task.getLastUpdate());
		cv.put("last_seen", task.getLastSeen());

		database.update("new_tasks", cv, "task_id = ?",
				new String[] { String.valueOf(task.getTaskID()) });
	}

	public MigratedTask getMigratedTask(String taskID) {
		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query("new_tasks", null, "task_id = ?",
				new String[] { taskID }, null, null, null);
		cursor.moveToFirst();

		MigratedTask migratedTask = getMigratedTaskFromCursor(cursor);
		cursor.close();
		return migratedTask;
	}
	
	public void deleteMigratedTask(String taskID) {
		SQLiteDatabase database = getWritableDatabase();

		database.delete("new_tasks", "task_id = ?",
			new String[] { String.valueOf(taskID) });
	}
	
	public ArrayList<MigratedTask> getMigratedTasksForEmployee(String employeeID) {
		SQLiteDatabase database = getReadableDatabase();
		
		ArrayList<MigratedTask> tasks = new ArrayList<MigratedTask>();
		
		if (employeeID == null) {
			return tasks;
		}

		Cursor cursor = database.query("new_tasks", null, "employee_id = ?",
				new String[] { employeeID }, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			MigratedTask task = getMigratedTaskFromCursor(cursor);
			tasks.add(task);
			cursor.moveToNext();
		}
		cursor.close();
		return tasks;
	}


	public void deleteAllMigratedTasks(String employeeID) {
		SQLiteDatabase database = getReadableDatabase();

		database.delete("new_tasks", "employee_id = ?", new String[] { employeeID });
	}

	public ArrayList<MigratedTask> getMigratedTasksForUser(String userID) {
		SQLiteDatabase database = getReadableDatabase();
		
		ArrayList<MigratedTask> tasks = new ArrayList<MigratedTask>();

		Cursor cursor = database.query("new_tasks", null, "user_id = ?",
				new String[] { userID }, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			MigratedTask task = getMigratedTaskFromCursor(cursor);
			tasks.add(task);
			cursor.moveToNext();
		}
		cursor.close();
		return tasks;
	}
	
	public void deleteAllMigratedTasksForUser(String userID) {
		SQLiteDatabase database = getReadableDatabase();

		database.delete("new_tasks", "user_id = ?", new String[] { userID });
	}
	
	public void setMigratedEmployee(MigratedEmployee employee) {
		SQLiteDatabase database = getWritableDatabase();
		boolean newEmployeesTableExists = isTableExisting("new_employees", database);
		
		if (!newEmployeesTableExists) {
			database.execSQL("create table new_employees (name varchar(100), "
					+ "phone varchar(100), company_id varchar(100), user_id varchar(100),"
					+ "employee_id varchar(100), designation varchar(256), task_count varchar(100), last_updated varchar(100))"); 
		}

		ContentValues cv = new ContentValues();

		cv.put("user_id", employee.getUserID());
		cv.put("company_id", employee.getCompanyID());
		cv.put("employee_id", employee.getEmployeeID());
		cv.put("designation", employee.getDesignation());
		cv.put("name", employee.getName());
		cv.put("phone", employee.getPhoneWithCountryCode());
		cv.put("task_count", employee.getTaskCount());
		cv.put("last_updated", employee.getLastUpdated());

		database.insert("new_employees", null, cv);
	}
	
	public boolean isTableExisting(String tableName, SQLiteDatabase database) {
		Cursor cursor = database.rawQuery(
				"select DISTINCT tbl_name from sqlite_master where tbl_name = '"
						+ tableName + "'", null);
		
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.close();
				return true;
			}
			cursor.close();
		}
		return false;
	}

	public void updateMigratedEmployee(MigratedEmployee employee) {
		SQLiteDatabase database = getWritableDatabase();

		ContentValues cv = new ContentValues();

		cv.put("user_id", employee.getUserID());
		cv.put("company_id", employee.getCompanyID());
		cv.put("employee_id", employee.getEmployeeID());
		cv.put("designation", employee.getDesignation());
		cv.put("name", employee.getName());
		cv.put("phone", employee.getPhoneWithCountryCode());

		database.update("new_employees", cv, "employee_id = ?",
				new String[] { String.valueOf(employee.getEmployeeID()) });
	}

	public void deleteMigratedEmployee(MigratedEmployee employee) {
		deleteMigratedEmployee(employee.getEmployeeID());
	}
	
	public void deleteMigratedEmployee(String empId) {
		SQLiteDatabase database = getWritableDatabase();

		database.delete("new_employees", "employee_id = ?",
			new String[] { empId });
	}

    public MigratedEmployee getMigratedEmployeeByPhoneAndName(String number, String name) {
        if (number == null) {
            return null;
        }

        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query("new_employees", null, "phone = ? AND name = ?",
                new String[] { number, name }, null, null, null);

        if (cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToFirst();
        MigratedEmployee employee = getMigratedEmployeeFromCursor(cursor);
        cursor.close();
        return employee;
    }
	
	public MigratedEmployee getMigratedEmployee(String employeeID) {
        if (employeeID == null) {
            return null;
        }

		SQLiteDatabase database = getReadableDatabase();

		Cursor cursor = database.query("new_employees", null, "employee_id = ?",
				new String[] { employeeID }, null, null, null);
		
		if (cursor.getCount() == 0) {
			return null;
		}
		
		cursor.moveToFirst();
		MigratedEmployee migratedEmployee = getMigratedEmployeeFromCursor(cursor);
		cursor.close();
		return migratedEmployee;
	}
	
	public ArrayList<MigratedEmployee> getMigratedEmployees() {
		SQLiteDatabase database = getReadableDatabase();
		
		ArrayList<MigratedEmployee> employees = new ArrayList<MigratedEmployee>();

		Cursor cursor = database.query("new_employees", null, null, null,
				null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			MigratedEmployee employee = getMigratedEmployeeFromCursor(cursor);
			employees.add(employee);
			cursor.moveToNext();
		}
		cursor.close();
		return employees;
	}


	public void deleteAllMigratedEmployees() {
		SQLiteDatabase database = getReadableDatabase();
		boolean newEmployeesTableExists = isTableExisting("new_employees",
				database);
		if (newEmployeesTableExists) {
			database.delete("new_employees", null, null);
		}
	}
	
	public ArrayList<MigratedMessage> getMigratedMessagesForTask(String taskID) {
		SQLiteDatabase database = getReadableDatabase();
		
		ArrayList<MigratedMessage> messages = new ArrayList<MigratedMessage>();

		Cursor cursor = database.query("new_messages", null, "task_id = ?",
				new String[] { taskID }, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			MigratedMessage message = getMigratedMessageFromCursor(cursor);
			messages.add(message);
			cursor.moveToNext();
		}
		cursor.close();
		return messages;
	}

    public MigratedTask getMigratedTaskForTitleAndEmployee(String taskTitle, String empNumber) {
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query("new_tasks", null, "title = ? AND employee_number = ?",
                new String[] { taskTitle, empNumber }, null, null, null);
        cursor.moveToFirst();
        MigratedTask migratedTask = getMigratedTaskFromCursor(cursor);
        cursor.close();

        return migratedTask;
    }
	
	public void deleteAllMigratedMessagesForTask(String taskID) { 
		SQLiteDatabase database = getWritableDatabase();

		database.delete("new_messages", "task_id = ?",
			new String[] { taskID });
	}

	
	private MigratedMessage getMigratedMessageFromCursor(Cursor cursor) {
		MigratedMessage migratedMessage = new MigratedMessage();
		migratedMessage.setEmployeeID(cursor.getString(cursor
				.getColumnIndex("employee_id")));
		migratedMessage.setText(cursor.getString(cursor
				.getColumnIndex("text")));
		migratedMessage.setTaskID(cursor.getString(cursor
				.getColumnIndex("task_id")));
		migratedMessage.setMessageID(cursor.getString(cursor
				.getColumnIndex("message_id")));
		migratedMessage.setTime(cursor.getLong(cursor
				.getColumnIndex("time")));
		return migratedMessage;
	}
	
	private MigratedEmployee getMigratedEmployeeFromCursor(Cursor cursor) {
		MigratedEmployee migratedEmployee = new MigratedEmployee();
		migratedEmployee.setEmployeeID(cursor.getString(cursor
				.getColumnIndex("employee_id")));
		migratedEmployee.setName(cursor.getString(cursor
				.getColumnIndex("name")));
		migratedEmployee.setPhoneWithContryCode(cursor.getString(cursor
				.getColumnIndex("phone")));
		migratedEmployee.setUserID(cursor.getString(cursor
				.getColumnIndex("user_id")));
		migratedEmployee.setCompanyID(cursor.getString(cursor
				.getColumnIndex("company_id")));
		migratedEmployee.setDesignation(cursor.getString(cursor
				.getColumnIndex("designation")));
		migratedEmployee.setTaskCount(cursor.getString(cursor
				.getColumnIndex("task_count")));
		migratedEmployee.setLastUpdated(cursor.getString(cursor
				.getColumnIndex("last_updated")));
		return migratedEmployee;
	}

	
	private MigratedTask getMigratedTaskFromCursor(Cursor cursor) {
        if (cursor.getCount() <= 0) {
            return null;
        }

		MigratedTask migratedTask = new MigratedTask();
		migratedTask.setTaskID(cursor.getString(cursor
				.getColumnIndex("task_id")));
		migratedTask.setEmployeeID(cursor.getString(cursor
				.getColumnIndex("employee_id")));
		migratedTask.setEmployeeName(cursor.getString(cursor
				.getColumnIndex("employee_name")));
		migratedTask.setEmployeeNumber(cursor.getString(cursor
				.getColumnIndex("employee_number")));
		migratedTask.setCompanyID(cursor.getString(cursor
				.getColumnIndex("company_id")));
		migratedTask.setUserID(cursor.getString(cursor
				.getColumnIndex("user_id")));
		migratedTask.setDescription(cursor.getString(cursor
				.getColumnIndex("description")));
		migratedTask.setTitle(cursor.getString(cursor
				.getColumnIndex("title")));
		migratedTask.setUpdateCount(cursor.getLong(cursor
				.getColumnIndex("update_count")));
		migratedTask.setStatus(cursor.getString(cursor
				.getColumnIndex("status")));
		migratedTask.setFrequency(cursor.getLong(cursor
				.getColumnIndex("frequency")));
		migratedTask.setCreatedOn(cursor.getLong(cursor
				.getColumnIndex("created_on")));
		migratedTask.setLastReminder(cursor.getLong(cursor
				.getColumnIndex("last_reminder")));
		migratedTask.setLastUpdate(cursor.getLong(cursor
				.getColumnIndex("last_update")));
		migratedTask.setLastSeen(cursor.getLong(cursor
				.getColumnIndex("last_seen")));
		return migratedTask;
	}


	private EmployeeState getEmployeeStateFromCursor(Cursor cursor) {
		EmployeeState employeeState = new EmployeeState();
		employeeState.setLastTaskSentID(cursor.getLong(cursor
				.getColumnIndex("last_sent_task")));
		employeeState.setReplyReceived(cursor.getShort(cursor
				.getColumnIndex("reply_received")) > 0);
		employeeState.setTaskSentTime(cursor.getLong(cursor
				.getColumnIndex("task_sent_time")));
		employeeState.setRemindersSent(cursor.getInt(cursor
				.getColumnIndex("reminders_sent")));
		employeeState.setTaskSentReminderTime(cursor.getLong(cursor
				.getColumnIndex("task_reminder_start_time")));
		employeeState.setEmployeeID(cursor.getString(cursor
				.getColumnIndex("employee_id")));

		return employeeState;
	}

	private Employee getEmployeeFromCursor(Cursor cursor) {
		Employee employee = new Employee();
		employee.setId(cursor.getString(cursor.getColumnIndex("_id")));
		employee.setNumber(cursor.getString(cursor.getColumnIndex("number")));
		employee.setName(cursor.getString(cursor.getColumnIndex("name")));
		return employee;
	}

	private Task getTaskFromCursor(Cursor cursor) {
		Task task = new Task();
		task.setId(cursor.getLong(cursor.getColumnIndex("_id")));
		task.setCompleted(cursor.getInt(cursor.getColumnIndex("completed")) > 0);
		task.setEmployeeID(cursor.getString(cursor.getColumnIndex("employee_id")));
		task.setDescription(cursor.getString(cursor
				.getColumnIndex("description")));
		task.setFrequency(cursor.getLong(cursor.getColumnIndex("frequency")));
		task.setLastSent(cursor.getLong(cursor.getColumnIndex("last_sent_time")));
		task.setLastSeenTime(cursor.getLong(cursor
				.getColumnIndex("last_seen_time")));
		
		task.setRepeated(cursor.getInt(cursor.getColumnIndex("is_repeated")) > 0);
		task.setRepeatFrequency(cursor.getLong(cursor.getColumnIndex("repeat_frequency")));
		task.setRepeatDay(cursor.getLong(cursor.getColumnIndex("repeat_day")));
		task.setCompletedCurrent(cursor.getInt(cursor.getColumnIndex("completed_current")) > 0);

		return task;
	}

	private Reply getReplyFromCursor(Cursor cursor) {
		Reply reply = new Reply();

		reply.setReplyText(cursor.getString(cursor.getColumnIndex("reply_text")));
		reply.setReceivedTime(cursor.getLong(cursor
				.getColumnIndex("received_time")));
		reply.setEmployeeID(cursor.getString(cursor.getColumnIndex("employee_id")));
		reply.setTaskID(cursor.getLong(cursor.getColumnIndex("task_id")));
		reply.setId(cursor.getLong(cursor.getColumnIndex("_id")));
		return reply;
	}
	
}
