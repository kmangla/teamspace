package com.android.teamspace.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MockNetworkData {
	
	private static int counter = 0;
	
	public static boolean shouldMockNetworkData() {
		return false;
	}

	public static JSONArray getJSONArrayResponse(String url) {		
		if (url.contains("?employeeID=") && url.contains("api/tasks")) {
			// Tasks for a particular employee (assume employee_1)
			JSONArray tasks = new JSONArray();
			try {
				JSONObject tempTask = new JSONObject();
				counter++;
				tempTask.put("description", counter + " Replace production floor conveyer belt");
				tempTask.put("title", "Title 1");
				tempTask.put("updateCount", "10");
				tempTask.put("status", "complete");
				tempTask.put("taskID", "task_1");
				tempTask.put("userID", "user_1");
				tempTask.put("companyID", "company_1");
				tempTask.put("employeeID", "employee_1");
				tempTask.put("createdOn", "2014-12-27T11:19:42.704Z");
				tempTask.put("lastUpdate", "2014-12-27T11:19:42.704Z");
				tempTask.put("frequency", 86400);
				tasks.put(tempTask);
				
				tempTask = new JSONObject();
				counter++;
				tempTask.put("description", counter + " Install safety valve");
				tempTask.put("title", "Title 2");
				tempTask.put("updateCount", "12");
				tempTask.put("status", "in progress");
				tempTask.put("taskID", "task_2");
				tempTask.put("userID", "user_1");
				tempTask.put("companyID", "company_1");
				tempTask.put("employeeID", "employee_1");
				tempTask.put("createdOn", "2014-12-28T11:19:42.704Z");
				tempTask.put("lastUpdate", "2014-12-28T11:19:42.704Z");
				tempTask.put("frequency", 604800);
				tasks.put(tempTask);
				
			} catch (JSONException e) {
				Log.e("MockNetworkData 1 json_parsing_exception", e.getMessage());
			}
			return tasks;			
		} else if (url.contains("api/tasks")) {
			// All tasks for a particular owner / user
			JSONArray tasks = new JSONArray();
			try {
				JSONObject tempTask = new JSONObject();
				counter++;
				tempTask.put("description", counter + " Replace production floor conveyer belt");
				tempTask.put("title", "Title 3");
				tempTask.put("updateCount", "10");
				tempTask.put("status", "complete");
				tempTask.put("taskID", "task_1");
				tempTask.put("userID", "user_1");
				tempTask.put("companyID", "company_1");
				tempTask.put("employeeID", "employee_3");
				tempTask.put("createdOn", "2014-12-27T11:19:42.704Z");
				tempTask.put("lastUpdate", "2014-12-27T11:19:42.704Z");
				tempTask.put("frequency", 604800);
				tasks.put(tempTask);
				
				tempTask = new JSONObject();
				counter++;
				tempTask.put("description", counter + " Install safety valve");
				tempTask.put("title", "Title 4");
				tempTask.put("updateCount", "12");
				tempTask.put("status", "in progress");
				tempTask.put("taskID", "task_2");
				tempTask.put("userID", "user_1");
				tempTask.put("companyID", "company_1");
				tempTask.put("employeeID", "employee_1");
				tempTask.put("createdOn", "2014-12-28T11:19:42.704Z");
				tempTask.put("lastUpdate", "2014-12-28T11:19:42.704Z");
				tempTask.put("frequency", 86400);
				tasks.put(tempTask);
				
				tempTask = new JSONObject();
				counter++;
				tempTask.put("description", counter + " File income tax");
				tempTask.put("title", "Title 5");
				tempTask.put("updateCount", "12");
				tempTask.put("status", "in progress");
				tempTask.put("taskID", "task_3");
				tempTask.put("userID", "user_1");
				tempTask.put("companyID", "company_1");
				tempTask.put("employeeID", "employee_2");
				tempTask.put("createdOn", "2014-12-29T11:19:42.704Z");
				tempTask.put("lastUpdate", "2014-12-29T11:19:42.704Z");
				tempTask.put("frequency", 86400);
				tasks.put(tempTask);
				
				tempTask = new JSONObject();
				counter++;
				tempTask.put("description", counter + " Install antivirus software");
				tempTask.put("title", "Title 6");
				tempTask.put("updateCount", "14");
				tempTask.put("status", "in progress");
				tempTask.put("taskID", "task_4");
				tempTask.put("userID", "user_1");
				tempTask.put("companyID", "company_1");
				tempTask.put("employeeID", "employee_2");
				tempTask.put("createdOn", "2014-12-18T11:19:42.704Z");
				tempTask.put("lastUpdate", "2014-12-18T11:19:42.704Z");
				tempTask.put("frequency", 86400);
				tasks.put(tempTask);
				
			} catch (JSONException e) {
				Log.e("MockNetworkData 2 json_parsing_exception", e.getMessage());
			}
			return tasks;
		} else {
			JSONArray employees = new JSONArray();
			try {
				JSONObject tempEmp = new JSONObject();
				counter++;
				tempEmp.put("name", "Vivek Tripathi " + counter);
				tempEmp.put("phone", "+16506447351");
				tempEmp.put("companyID", "company_1");				
				tempEmp.put("userID", "user_1");
				tempEmp.put("employeeID", "employee_1");
				tempEmp.put("designation", "Engineer");	
				tempEmp.put("taskCount", "10");	
				tempEmp.put("lastUpdated", "2");	
				employees.put(tempEmp);
				
				tempEmp = new JSONObject();
				counter++;
				tempEmp.put("name", "Karan Mangla " + counter);
				tempEmp.put("phone", "+919992222222");
				tempEmp.put("companyID", "company_1");				
				tempEmp.put("userID", "user_1");
				tempEmp.put("employeeID", "employee_2");
				tempEmp.put("designation", "Sr. Manager");		
				tempEmp.put("taskCount", "11");
				tempEmp.put("lastUpdated", "3");
				employees.put(tempEmp);
				
				tempEmp = new JSONObject();
				counter++;
				tempEmp.put("name", "Pratyus Patnaik " + counter);
				tempEmp.put("phone", "+14082504123");
				tempEmp.put("companyID", "company_1");				
				tempEmp.put("userID", "user_1");
				tempEmp.put("employeeID", "employee_3");
				tempEmp.put("designation", "Manager");	
				tempEmp.put("taskCount", "12");
				tempEmp.put("lastUpdated", "1");
				employees.put(tempEmp);
			} catch (JSONException e) {
				Log.e("MockNetworkData 3 json_parsing_exception", e.getMessage());
			}
			return employees;
		}
	}
}
