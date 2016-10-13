package com.yf.wh.library.download.asynctask;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Pair;


public class AsyncLoadTask extends AsyncTask<Integer, Void, Pair<Integer, Bitmap>> {

	public AsyncLoadTask() {
		super();
	}

	@Override protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override protected void onPostExecute(Pair<Integer, Bitmap> integerBitmapPair) {
		super.onPostExecute(integerBitmapPair);
	}

	@Override protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
	}

	@Override protected void onCancelled(Pair<Integer, Bitmap> integerBitmapPair) {
		super.onCancelled(integerBitmapPair);
	}

	@Override protected void onCancelled() {
		super.onCancelled();
	}

	@Override protected Pair<Integer, Bitmap> doInBackground(Integer... params) {
		return null;
	}
}
