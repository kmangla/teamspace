package com.android.teamspace.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.android.teamspace.caching.DatabaseCache;

import android.content.Context;
import android.util.Log;

public class EmployeeScoreRanking {

	public static ArrayList<EmployeeScore> getRankedEmployees(Context context,
			int numDays) {
		DatabaseCache dataManager = DatabaseCache.getInstance(context);
		ArrayList<Task> completedTasks = dataManager.getCompletedTasks(numDays);
		ArrayList<Task> incompleteTasks = dataManager.getIncompletedTasks();
		ArrayList<Reply> replies = dataManager.getAllReplies(numDays);

		HashMap<String, Long> employeeCompletedTasksCount = getEmployeeToCompletedTaskCount(completedTasks);
		HashMap<String, Long> employeeIncompletedTasksCount = getEmployeeToIncompletedTaskCount(incompleteTasks);
		HashMap<String, Long> employeeRepliedTasksCount = getEmployeeToRepliedTaskCount(
				context, replies);

		ArrayList<Employee> employees = dataManager.getEmployees();

		ArrayList<EmployeeScore> employeeScores = new ArrayList<EmployeeScore>();

		Iterator<Employee> it = employees.iterator();
		while (it.hasNext()) {
			Employee e = it.next();
			EmployeeScore score = new EmployeeScore();
			score.setEmployeeID(e.getId());

			if (employeeCompletedTasksCount.containsKey(e.getId())) {
				Long completedTasksCount = employeeCompletedTasksCount.get(e
						.getId());
				score.setNumClosed(completedTasksCount);
			} else {
				score.setNumClosed(0);
			}

			if (employeeIncompletedTasksCount.containsKey(e.getId())) {
				Long incompletedTasksCount = employeeIncompletedTasksCount
						.get(e.getId());
				score.setNumOpenTasks(incompletedTasksCount);
			} else {
				score.setNumOpenTasks(0);
			}

			if (employeeRepliedTasksCount.containsKey(e.getId())) {
				Long repliedTasksCount = employeeRepliedTasksCount.get(e
						.getId());
				score.setNumUpdates(repliedTasksCount);
			} else {
				score.setNumUpdates(0);
			}

			employeeScores.add(score);
		}
		Collections.sort(employeeScores, Collections.reverseOrder());
		return employeeScores;
	}

	private static HashMap<String, Long> getEmployeeToCompletedTaskCount(
			ArrayList<Task> completedTasks) {
		HashMap<String, Long> employeeCompletedTasks = new HashMap<String, Long>();
		Iterator<Task> it = completedTasks.iterator();
		while (it.hasNext()) {
			Task t = it.next();
			if (employeeCompletedTasks.containsKey(t.getEmployeeID())) {
				long score = employeeCompletedTasks.get(t.getEmployeeID());
				employeeCompletedTasks.put(t.getEmployeeID(), score + 1);
			} else {
				employeeCompletedTasks.put(t.getEmployeeID(), Long.valueOf(1));
			}
		}
		return employeeCompletedTasks;
	}

	private static HashMap<String, Long> getEmployeeToIncompletedTaskCount(
			ArrayList<Task> incompleteTasks) {
		HashMap<String, Long> employeeIncompletedTasks = new HashMap<String, Long>();
		Iterator<Task> it = incompleteTasks.iterator();
		while (it.hasNext()) {
			Task t = it.next();
			if (employeeIncompletedTasks.containsKey(t.getEmployeeID())) {
				long score = employeeIncompletedTasks.get(t.getEmployeeID());
				employeeIncompletedTasks.put(t.getEmployeeID(), score + 1);
			} else {
				employeeIncompletedTasks
						.put(t.getEmployeeID(), Long.valueOf(1));
			}
		}
		return employeeIncompletedTasks;
	}

	private static HashMap<String, Long> getEmployeeToRepliedTaskCount(
			Context context, ArrayList<Reply> replies) {
		HashMap<String, HashSet<Long>> employeeRepliedTasks = new HashMap<String, HashSet<Long>>();
		Iterator<Reply> it = replies.iterator();
		while (it.hasNext()) {
			Reply r = it.next();
			if (employeeRepliedTasks.containsKey(r.getEmployeeID())) {
				HashSet<Long> set = employeeRepliedTasks.get(r.getEmployeeID());
				set.add(Long.valueOf(r.getTaskID()));
			} else {
				HashSet<Long> set = new HashSet<Long>();
				set.add(Long.valueOf(r.getTaskID()));
				employeeRepliedTasks.put(r.getEmployeeID(), set);
			}
		}
		HashMap<String, Long> employeeRepliedTasksCount = new HashMap<String, Long>();

		Iterator<Map.Entry<String, HashSet<Long>>> itReplies = employeeRepliedTasks
				.entrySet().iterator();
		while (itReplies.hasNext()) {
			Map.Entry<String, HashSet<Long>> pairs = (Map.Entry<String, HashSet<Long>>) itReplies
					.next();
			employeeRepliedTasksCount.put(pairs.getKey(),
					Long.valueOf(((HashSet<Long>) pairs.getValue()).size()));
		}

		return employeeRepliedTasksCount;
	}

}
