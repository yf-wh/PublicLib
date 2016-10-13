package com.yf.wh.library.common;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;


public class WeatherUpdateManager {
	private static WeatherUpdateManager instance;
	
	private Timer weatherTimer;
	private ArrayList<WeatherUpdateListener> listeners = new ArrayList<>(); 

	public static synchronized WeatherUpdateManager getInstance() {
		if (instance == null) {
			synchronized (WeatherUpdateManager.class) {
				instance = new WeatherUpdateManager();
			}
		}
		return instance;
	}
	
	public void addWeatherUpdateListener(WeatherUpdateListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
		getWeatherData();
	}
	
	public void removeWeatherUpdateListener(WeatherUpdateListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}
	
	public void exeUpdateWeatherDate() {
		for (WeatherUpdateListener listener : listeners) {
			listener.updateWeatherData();
		}
	}
	
	public void getWeatherData() {
		if (weatherTimer != null) {
			weatherTimer.cancel();
		}
		weatherTimer = new Timer();
		weatherTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
//					Gson gson = new Gson();
//					String sinaCity = WeatherUtil
//							.getWebContent2("http://int.dpool.sina.com.cn/iplookup/iplookup.php");
//					Log.d("info " + sinaCity, ", size:" + sinaCity.length());
//
//					String cut = sinaCity.substring(0, sinaCity.length() - 4);
//					String city = cut.substring(cut.lastIndexOf('\t') + 1,
//							cut.length());
//					log.d("sinaCity = " + city);
//
//					String weatherJson = WeatherUtil
//							.getWebContent("http://wthrcdn.etouch.cn/weather_mini?city="
//									+ city);
//					WeatherUtil.bean = gson.fromJson(weatherJson,
//							WeatherBean.class);
//					log.d(WeatherUtil.bean.toString());
					exeUpdateWeatherDate();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1000, 30 * 60 * 1000);
	}
	
	
	public interface WeatherUpdateListener {

		void updateWeatherData();
	}
}
