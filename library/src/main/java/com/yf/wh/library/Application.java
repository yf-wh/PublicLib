package com.yf.wh.library;

public class Application extends android.app.Application {

	private static Application mInstance = null;

	public synchronized static Application getInstance() {
		return mInstance;
	}

	public synchronized static Application getContext() {
		return mInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}

}
