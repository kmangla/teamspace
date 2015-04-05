package com.ts.messagespace;

import android.content.Context;

public class NetworkRoutes {
	public static final String ROUTE_ALL_TASKS = "/tasks";
	public static final String ROUTE_TASK = "/task";
	public static final String ROUTE_EMPLOYEES = "/employees";
	public static final String ROUTE_EMPLOYEE = "/employee";
	public static final String ROUTE_MESSAGES = "/messages";
	public static final String ROUTE_MESSAGE = "/message";
    public static final String ROUTE_TOKEN = "/token";

    public static String getRouteBase() {
        Context context = MessageSpaceApplication.getAppContext();
        return "http://" + Utils.getServer(context) + Utils.getPort(context);
    }
}
