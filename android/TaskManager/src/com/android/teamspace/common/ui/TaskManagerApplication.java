package com.android.teamspace.common.ui;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;

import com.android.teamspace.caching.DatabaseCache;
import com.android.teamspace.caching.dataupdaters.TaskUpdater;
import com.android.teamspace.models.Employee;
import com.android.teamspace.models.EmployeeState;
import com.android.teamspace.models.MigratedTask;
import com.android.teamspace.models.Reply;
import com.android.teamspace.models.Task;

public class TaskManagerApplication extends Application {

	DatabaseCache taskManagerData;
	private static Context appContext;

	private long lastRunTime = 0;

	public long getLastRunTime() {
		return lastRunTime;
	}

	public void setLastRunTime(long lastRunTime) {
		this.lastRunTime = lastRunTime;
	}
	
	public static Context getAppContext() {
        return TaskManagerApplication.appContext;
    }

	@Override
	public void onCreate() {
		super.onCreate();
		
		appContext = this.getApplicationContext();
		taskManagerData = DatabaseCache.getInstance(this
				.getApplicationContext());
		
		// Sending reminder SMS will be done by server.
//		Intent alarmIntent = new Intent(this.getApplicationContext(),
//				TaskUpdater.class);
//		Intent repeatedTaskIntent = new Intent(this.getApplicationContext(),
//				RepeatedTaskChecker.class);
//		PendingIntent pendingIntent = PendingIntent.getBroadcast(
//				this.getApplicationContext(), 0, alarmIntent,
//				PendingIntent.FLAG_UPDATE_CURRENT);
//		PendingIntent repeatedTaskChecker = PendingIntent.getBroadcast(
//				this.getApplicationContext(), 0, repeatedTaskIntent,
//				PendingIntent.FLAG_UPDATE_CURRENT);
//		AlarmManager alarmManager = (AlarmManager) this.getApplicationContext()
//				.getSystemService(Context.ALARM_SERVICE);
//		alarmManager.setInexactRepeating(AlarmManager.RTC,
//		   Calendar.getInstance().getTimeInMillis(),
//		   AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
//		alarmManager.setInexactRepeating(AlarmManager.RTC,
//				   Calendar.getInstance().getTimeInMillis(),
//				   AlarmManager.INTERVAL_DAY, repeatedTaskChecker);
//		final Context context = this.getApplicationContext();
//		MigratedTask task = new MigratedTask();
//		task.setDescription("task1");
//		task.setTitle("task1title");
//		task.setStatus("complete");
//		task.setUserID("user_549e951d9c8cfe7960f935aa");
//		task.setCompanyID("1");
//		task.setEmployeeID("employee_549e957c7d260f8460dcffbc");
//		task.setFrequency(86400);
//		task.setCreatedOn(System.currentTimeMillis());
//		TaskUpdater updater = new TaskUpdater(context, task, null);
//		updater.create();
		
		/*
		DataManager.getInstance(context).fetchTasksForEmployee("employee_549e957c7d260f8460dcffbc",
				new DataManagerCallback() {

					@Override
					public void onDataReceivedFromCache(String dataStoreKey) {
						if (dataStoreKey == null) {
							return;
						}
						ArrayList<MigratedTask> data = (ArrayList<MigratedTask>) DataManager
								.getInstance(context)
								.retrieveData(dataStoreKey);
						Log.i("karan_debug", "tasks_for_employee_db " + data);
						}

					@Override
					public void onDataReceivedFromServer(String dataStoreKey) {
						if (dataStoreKey == null) {
							return;
						}
						ArrayList<MigratedTask> data = (ArrayList<MigratedTask>) DataManager
								.getInstance(context)
								.retrieveData(dataStoreKey);
						Log.i("karan_debug", "tasks_for_employee_network" + data);
					}

				});
		DataManager.getInstance(context).fetchTasksForUser("user_549e951d9c8cfe7960f935aa",
				new DataManagerCallback() {

					@Override
					public void onDataReceivedFromCache(String dataStoreKey) {
						if (dataStoreKey == null) {
							return;
						}
						ArrayList<MigratedTask> data = (ArrayList<MigratedTask>) DataManager
								.getInstance(context)
								.retrieveData(dataStoreKey);
						Log.i("karan_debug", "tasks_for_user_db" + data);
						}

					@Override
					public void onDataReceivedFromServer(String dataStoreKey) {
						if (dataStoreKey == null) {
							return;
						}
						ArrayList<MigratedTask> data = (ArrayList<MigratedTask>) DataManager
								.getInstance(context)
								.retrieveData(dataStoreKey);
						Log.i("karan_debug", "tasks_for_user_network" + data);
					}

				});
		DataManager.getInstance(context).fetchEmployeesForUser("user_549e951d9c8cfe7960f935aa",
				new DataManagerCallback() {

					@Override
					public void onDataReceivedFromCache(String dataStoreKey) {
						if (dataStoreKey == null) {
							return;
						}
						ArrayList<MigratedEmployee> data = (ArrayList<MigratedEmployee>) DataManager
								.getInstance(context)
								.retrieveData(dataStoreKey);
						Log.i("karan_debug", "employees_for_user_db" + data);
						}

					@Override
					public void onDataReceivedFromServer(String dataStoreKey) {
						if (dataStoreKey == null) {
							return;
						}
						ArrayList<MigratedEmployee> data = (ArrayList<MigratedEmployee>) DataManager
								.getInstance(context)
								.retrieveData(dataStoreKey);
						Log.i("karan_debug", "employees_for_user_network" + data);
					}

				});*/

	}

	public ArrayList<Employee> getEmployees() { 
		return taskManagerData.getEmployees();
	}

	public Employee getEmployee(String employeeId) {
		return taskManagerData.getEmployee(employeeId);
	}

	public ArrayList<Task> getTasks(String employee) {
		return taskManagerData.getTasks(employee);
	}

	public ArrayList<Task> getCompletedTasks(String employee) {
		return taskManagerData.getCompletedTasks(employee);
	}

	public Task getTaskByID(long taskID) {
		return taskManagerData.getTaskByID(taskID);
	}

	public Employee getEmployeeByPhone(String number) {
		return taskManagerData.getEmployeeByPhone(number);
	}

	public EmployeeState getEmployeeState(String employeeID) {
		return taskManagerData.getEmployeeState(employeeID);
	}

	public ArrayList<Reply> getRepliesForTask(long taskID) {
		return taskManagerData.getRepliesForTask(taskID);
	}

	public boolean addTask(Task task) {
		return taskManagerData.addTask(task);
	}

	public boolean updateEmployee(Employee employee) {
		return taskManagerData.updateEmployee(employee);
	}

	public boolean addEmployee(Employee employee) {
		return taskManagerData.addEmployee(employee);
	}

	public void deleteEmployee(Employee employee) {
		taskManagerData.deleteEmployee(employee);
	}

	public int getUnreadRepliesForTask(Task task) {
		return taskManagerData.getUnreadRepliesForTask(task);
	}

	public int getPendingTasksCount(Employee employee) {
		return taskManagerData.getPendingTasksCount(employee);
	}

	public int getUnreadRepliesForEmployee(Employee employee) {
		return taskManagerData.getUnreadRepliesForEmployee(employee);
	}

	public int getIncompleteTasksCount(Employee employee) {
		return taskManagerData.getIncompleteTasksCount(employee);
	}

	public boolean updateEmployeeState(EmployeeState employeeState,
			String employeeID) {
		return taskManagerData.updateEmployeeState(employeeState, employeeID);
	}

	public boolean updateTask(Task task) {
		return taskManagerData.updateTask(task);
	}

	public boolean insertTaskPause(Task task) {
		return taskManagerData.insertTaskPause(task);
	}

	public boolean removeTaskPause(Task task) {
		return taskManagerData.removeTaskPause(task);
	}

	public boolean updateTaskPause(Task task, long pausedTill) {
		return taskManagerData.updateTaskPause(task, pausedTill);
	}

	public boolean isTaskPaused(Task task) {
		return taskManagerData.isTaskPaused(task);
	}

}
