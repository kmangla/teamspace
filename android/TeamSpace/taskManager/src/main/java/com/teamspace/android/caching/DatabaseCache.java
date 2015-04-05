package com.teamspace.android.caching;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.teamspace.android.caching.dataupdaters.EmployeeUpdater;
import com.teamspace.android.models.Employee;
import com.teamspace.android.models.EmployeeState;
import com.teamspace.android.models.MigratedEmployee;
import com.teamspace.android.models.MigratedMessage;
import com.teamspace.android.models.MigratedTask;
import com.teamspace.android.models.Reply;
import com.teamspace.android.models.Task;
import com.teamspace.android.utils.TimeUtil;
import com.teamspace.android.utils.Utils;

public class DatabaseCache {
	private HashMap<Long, Employee> employees;
	private ArrayList<Employee> employeeList;

	private HashMap<Long, ArrayList<Task>> tasks;
	private HashMap<Long, Task> tasksByID;

	private HashMap<String, Employee> employeeByPhone;
	private HashMap<Long, EmployeeState> employeeState;

	private DatabaseHelper dbHelper;

	private static DatabaseCache instance;

	private DatabaseCache(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public static DatabaseCache getInstance(Context context) {
		if (null == instance) {
			instance = new DatabaseCache(context);
		}
		return instance;
	}

	public ArrayList<Employee> getEmployees() {
		return dbHelper.getAllEmployees();
	}

	public ArrayList<Reply> getRepliesForTask(long taskID) {
		return dbHelper.getRepliesForTask(taskID);
	}

	public Employee getEmployee(String employeeId) {
		return dbHelper.getEmployee(employeeId);
	}

	public ArrayList<Task> getTasks(String employeeID) {
		return dbHelper.getTasks(employeeID, false);
	}

	public ArrayList<Task> getCompletedTasks(String employeeID) {
		return dbHelper.getTasks(employeeID, true);
	}

	public Task getTaskByID(long taskID) {
		return dbHelper.getTaskByID(taskID);
	}

	public Employee getEmployeeByPhone(String number) {
		return dbHelper.getEmployeeByPhone(number);
	}

	public EmployeeState getEmployeeState(String employeeID) {
		return dbHelper.getEmployeeStateByEmployeeID(employeeID);
	}
	
	public ArrayList<EmployeeState> getAllEmployeeStates() {
		return dbHelper.getAllEmployeeStates();
	}

	public long addReply(Reply reply) {
		return dbHelper.insertReply(reply);
	}

	public boolean addTask(Task task) {
		return dbHelper.insertTask(task);
	}

	public boolean addEmployee(Employee employee) {
		return dbHelper.insertEmployee(employee);
	}

	public boolean updateEmployee(Employee employee) {
		return dbHelper.updateEmployee(employee);
	}

	public boolean insertTaskPause(Task task) {
		return dbHelper.insertTaskPause(task);
	}

	public boolean removeTaskPause(Task task) {
		return dbHelper.removeTaskPause(task);
	}

	public boolean updateTaskPause(Task task, long pausedTill) {
		return dbHelper.updateTaskPause(task, pausedTill);
	}

	public long taskPausedTill(Task task) {
		return dbHelper.getTaskPausedTill(task);
	}

	public boolean isTaskPaused(Task task) {
		long seconds = TimeUtil.currentTimeSec();
		long pausedTill = dbHelper.getTaskPausedTill(task);
		Log.i("test", seconds + "");
		Log.i("test", pausedTill + "");
		return seconds < pausedTill;
	}

	public void deleteEmployee(Employee employee) {
		dbHelper.deleteEmployee(employee);
	}

	public boolean updateTask(Task task) {
		return dbHelper.updateTask(task);
	}

	public int getUnreadRepliesForTask(Task task) {
		return dbHelper.getUnreadRepliesForTask(task);
	}

	public int getPendingTasksCount(Employee employee) {
		return dbHelper.getPendingTasksCount(employee);
	}

	public int getUnreadRepliesForEmployee(Employee employee) {
		return dbHelper.getUnreadRepliesForEmployee(employee);
	}

	public int getIncompleteTasksCount(Employee employee) {
		return dbHelper.getIncompleteTasksCount(employee);
	}

	public boolean updateEmployeeState(EmployeeState employeeState,
			String employeeID) {
		return dbHelper.updateEmployeeState(employeeState, employeeID);
	}

	public ArrayList<Reply> getAllReplies(int numDays) {
		return dbHelper.getAllReplies(numDays);
	}

	public ArrayList<Task> getCompletedTasks(int numDays) {
		return dbHelper.getCompletedTasks(numDays);
	}
	
	public ArrayList<Task> getRepeatedTasks(String employeeID) {
		return dbHelper.getRepeatedTasks(employeeID);
	}

	public ArrayList<Task> getAllRepeatedTasks() {
		return dbHelper.getAllRepeatedTasks();
	}
	
	public ArrayList<Task> getIncompletedTasks() {
		return dbHelper.getIncompletedTasks();
	}
		
	// All New APIs
	// Messages
	public MigratedMessage getMigratedMessage(String messageID) {
		return dbHelper.getMigratedMessage(messageID);
	}
	
	public void setMigratedMessage(MigratedMessage message) {
		dbHelper.setMigratedMessage(message);
	}

	public void updateMigratedMessage(MigratedMessage message) {
		dbHelper.updateMigratedMessage(message);
	}
	
	public void deleteMigratedMessage(MigratedMessage message) {
		dbHelper.deleteMigratedMessage(message);
	}

	// Tasks
	public MigratedTask getMigratedTaskBlockingCall(String taskID) {
		return dbHelper.getMigratedTask(taskID);
	}
	
	public void setMigratedTask(MigratedTask task) {
		dbHelper.setMigratedTask(task);
	}

	public void updateMigratedTask(MigratedTask task) {
		dbHelper.updateMigratedTask(task);
	}
	
	public void deleteMigratedTaskBlockingCall(String taskID) {
		dbHelper.deleteMigratedTask(taskID);
	}
	
	public void deleteMigratedTask(final Context context, final String taskID) {
		AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... taskIDs) {
				Utils.log("Deleting task with id: " + taskIDs[0]);
				DatabaseCache.getInstance(context).deleteMigratedTaskBlockingCall(taskID);
				return taskIDs[0];
			}
		};
		asyncTask.execute(taskID);
	}

	// Employees
	public MigratedEmployee getMigratedEmployeeBlockingCall(String employeeID) {
		return dbHelper.getMigratedEmployee(employeeID);
	}

    public MigratedEmployee getMigratedEmployeeByPhoneAndName(String number, String name) {
        return dbHelper.getMigratedEmployeeByPhoneAndName(number, name);
    }
	
	public void setMigratedEmployeeBlockingCall(MigratedEmployee employee) {
		dbHelper.setMigratedEmployee(employee);
	}
	
	public void updateMigratedEmployeeBlockingCall(MigratedEmployee employee) {
		dbHelper.updateMigratedEmployee(employee);
	}
	
	public void updateMigratedEmployee(final Context context, final MigratedEmployee employee) {
		AsyncTask<MigratedEmployee, Void, String> asyncTask = new AsyncTask<MigratedEmployee, Void, String>() {
			@Override
			protected String doInBackground(MigratedEmployee... employeeIDs) {
				DatabaseCache.getInstance(context).updateMigratedEmployeeBlockingCall(employeeIDs[0]);
				return employeeIDs[0].getEmployeeID();
			}
		};
		asyncTask.execute(employee);
	}
	
	public void deleteMigratedEmployeeBlockingCall(MigratedEmployee employee) {
		dbHelper.deleteMigratedEmployee(employee);
	}
	
	public void deleteMigratedEmployee(final Context context, final String empId) {
		AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... employeeIDs) {
				Utils.log("Deleting employee details for emp id: " + employeeIDs[0]);
				DatabaseCache.getInstance(context).deleteMigratedEmployeeBlockingCall(empId);
				return employeeIDs[0];
			}
		};
		asyncTask.execute(empId);
	}
	
	public void deleteMigratedEmployeeBlockingCall(String empId) {
		dbHelper.deleteMigratedEmployee(empId);
	}
	
	// Helper functions
	public ArrayList<MigratedTask> getMigratedTasksForEmployeeBlockingCall(String employeeID) {
		return dbHelper.getMigratedTasksForEmployee(employeeID);
	}	
	
	public void deleteAllMigratedTasksBlockingCall(String employeeID) { 
		dbHelper.deleteAllMigratedTasks(employeeID);
	}
	
	public ArrayList<MigratedTask> getMigratedTasksForUserBlockingCall(String userID) {
		return dbHelper.getMigratedTasksForUser(userID);
	}

	public void deleteAllMigratedTasksForUserBlockingCall(String userID) { 
		dbHelper.deleteAllMigratedTasksForUser(userID);
	}

	public ArrayList<MigratedEmployee> getMigratedEmployeesBlockingCall() {
		return dbHelper.getMigratedEmployees();
	}
	
	public void deleteAllMigratedEmployeesBlockingCall() { 
		dbHelper.deleteAllMigratedEmployees();
	}

	public ArrayList<MigratedMessage> getMigratedMessagesForTaskBlockingCall(String taskID) {
		return dbHelper.getMigratedMessagesForTask(taskID);
	}
	
	public void deleteAllMigratedMessagesForTaskBlockingCall(String taskID) { 
		dbHelper.deleteAllMigratedMessagesForTask(taskID);
	}

    public MigratedTask getMigratedTaskWithTitleAndEmployeeBlockingCall(String title, String employeeNumber) {
        if (title == null || employeeNumber == null) {
            return null;
        }

        return dbHelper.getMigratedTaskForTitleAndEmployee(title, employeeNumber);
    }
}
