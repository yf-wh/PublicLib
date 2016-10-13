package com.yf.wh.library.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;

import com.yf.wh.library.Application;

import java.io.File;
import java.io.FileInputStream;

public class ScreenSaverManager {

	private static ScreenSaverManager instance;

	private PowerManager pm;
	private PowerManager.WakeLock wakeLock;
	private PowerManager.WakeLock wakeProtectLock;

	private ScreenSaverManager() {
		pm = (PowerManager) Application.getContext().getSystemService(Context.POWER_SERVICE);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.setPriority(1000);
		Application.getContext().registerReceiver(receiver, filter);
	}

	public static synchronized ScreenSaverManager getInstance() {
		if (instance == null) {
			synchronized (ScreenSaverManager.class) {
				instance = new ScreenSaverManager();
			}
		}
		return instance;
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d("ScreenSaverManager", "action = " + action);
			if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				isShutDown = true;
			} else if (action.equals(Intent.ACTION_SCREEN_ON)) {
				isShutDown = false;
				if (isFirstfakeShutDown) {
					isFirstfakeShutDown = false;
				}else {
					fakeShutDown = false;
				}
			}
		}
	};

	public void init() {
		if (null == wakeProtectLock) {
			Log.d("ScreenSaverManager", "call init");
			wakeProtectLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bright");
			wakeProtectLock.acquire();
			if (null == wakeLock) {
				wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
						"bright");
			}
			wakeLock.acquire();
			releaseWakeLock();
		}
	}

	public boolean isScreenOn() {
		return pm.isScreenOn();
	}

	public void acquireWakeLock() {
		if (null == wakeLock) {
			wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
					"bright");
		}
		if (isShutDown && !fakeShutDown) {
			Log.d("ScreenSaverManager", "call acquireWakeLock");
			wakeLock.acquire();
		}
	}

	public void releaseWakeLock() {
		if (null != wakeLock && wakeLock.isHeld()) {
			Log.d("ScreenSaverManager", "call releaseWakeLock");
			wakeLock.release();
			wakeLock = null;
		}
	}

	private boolean isShutDown = false;
	private boolean fakeShutDown = false;
	private boolean isFirstfakeShutDown = true;

	public boolean isShutDown() { 
		return isShutDown;
	}

	private boolean hdmiIsConnect() {

		String hdmiIsconnect = "";
		try {
			File file = new File("/sys/class/display/HDMI/connect");
			FileInputStream fin = new FileInputStream(file);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			hdmiIsconnect = new String(buffer);
			fin.close();
		} catch (Exception e) {
		}
		return hdmiIsconnect.trim().equals("1") ? true : false;
	}

	public void fakeShutDown() {
		Log.d("ScreenSaverManager", "hdmiIsConnect() = " + hdmiIsConnect());
		if (!hdmiIsConnect()) {
			Log.d("ScreenSaverManager", "call fakeShutDown");
			fakeShutDown = true;
			releaseWakeLock();
			Application.getContext().sendBroadcast(
					new Intent().setAction("com.YF.YuanFang.BlankScreen"));
		}
	}

	public void fakeBoot() {
		Log.d("ScreenSaverManager", "call fakeBoot");
		fakeShutDown = false;
		acquireWakeLock();
		releaseWakeLock();
	}

}
