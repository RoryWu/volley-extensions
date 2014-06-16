package com.navercorp.volleyextensions.volleyer.util;

import android.util.Log;

public class VolleyerLog {
	public static final String TAG = "Volleyer";
	public static final int LEVEL_PRIORITY = Log.DEBUG;

	public static void debug(String format, Object... args) {
		if (isLoggable()) {
			Log.d(TAG, buildMessage(format, args));
		}
	}

	public static void debug(Throwable throwable, String format, Object... args) {
		if (isLoggable()) {
			Log.d(TAG, buildMessage(format, args), throwable);
		}
	}

	public static void info(String format, Object... args) {
		Log.i(TAG, buildMessage(format, args));
	}

	public static void info(Throwable throwable, String format, Object... args) {
		Log.i(TAG, buildMessage(format, args), throwable);
	}

	public static void warn(String format, Object... args) {
		Log.w(TAG, buildMessage(format, args));
	}

	public static void warn(Throwable throwable, String format, Object... args) {
		Log.w(TAG, buildMessage(format, args), throwable);
	}

	public static void error(String format, Object... args) {
		Log.e(TAG, buildMessage(format, args));
	}

	public static void error(Throwable throwable, String format, Object... args) {
		Log.e(TAG, buildMessage(format, args), throwable);
	}

	private static String buildMessage(String format, Object... args) {
		return String.format(format, args);
	}

	private static boolean isLoggable() {
		return Log.isLoggable(TAG, LEVEL_PRIORITY);
	}
}
