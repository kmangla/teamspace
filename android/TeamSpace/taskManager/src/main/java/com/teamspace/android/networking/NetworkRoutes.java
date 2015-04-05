package com.teamspace.android.networking;

import com.teamspace.android.utils.Utils;

public class NetworkRoutes {
	public static final String ROUTE_ALL_TASKS = "/tasks";
	public static final String ROUTE_TASK = "/task";
	public static final String ROUTE_EMPLOYEES = "/employees";
	public static final String ROUTE_EMPLOYEE = "/employee";
    public static final String ROUTE_OTP = "/generateOTP";
	public static final String ROUTE_MESSAGES = "/messages";
	public static final String ROUTE_MESSAGE = "/message";
    public static final String ROUTE_VERIFY_OTP = "/verifyOTP";
    public static final String ROUTE_TOKEN = "/token";

    public static String getRouteBase() {
        return "http://" + Utils.getServerIP() + Utils.getServerPort();
    }
}
