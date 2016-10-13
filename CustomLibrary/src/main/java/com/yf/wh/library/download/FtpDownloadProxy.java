package com.yf.wh.library.download;

import android.os.Environment;
import android.util.Log;

import com.yf.wh.library.download.asynctask.ThreadPoolManager;
import com.yf.wh.library.download.asynctask.ThreadPoolTask;

public class FtpDownloadProxy {

	private static String TAG = FtpDownloadProxy.class.getSimpleName();
	
	private FtpService ftpService;
	private static FtpDownloadProxy instance;
	private ThreadPoolManager threadPoolManager;
	private OnDownloadListener mDownloadListener;
	
    private long downloadFileSize = 0;
    private boolean isDownloading = false;
    private boolean isDownloadingApk = false;

	private FtpDownloadProxy() {
		ftpService = new FtpService(this);
		threadPoolManager = new ThreadPoolManager(ThreadPoolManager.TYPE_FIFO,
				5);
	}

	public static synchronized FtpDownloadProxy getInstance() {
		if (instance == null) {
			synchronized (FtpDownloadProxy.class) {
				if (instance == null)
					instance = new FtpDownloadProxy();
			}
		}
		return instance;
	}
	
	public long getDownloadFileSize() {
		return downloadFileSize;
	}

	public void setDownloadFileSize(long downloadFileSize) {
		this.downloadFileSize = downloadFileSize;
	}

	public boolean isDownloading() {
		return isDownloading;
	}

	public void setDownloading(boolean isDownloading) {
		this.isDownloading = isDownloading;
	}
	
	public boolean isDownloadingApk() {
		return isDownloadingApk;
	}

	private void setDownloadingApk(boolean isDownloadingApk) {
		this.isDownloadingApk = isDownloadingApk;
	}

	public void setDownloadListener(OnDownloadListener downloadListener) {
		mDownloadListener = downloadListener;
	}

	public void downStart(long downloadFileSize) {
		setDownloadFileSize(downloadFileSize);
		setDownloading(true);
		if (mDownloadListener != null) {
			mDownloadListener.onDownloadStart(downloadFileSize);
		}
	}

	public void downloading(long progress) {
		if (mDownloadListener != null) {
			mDownloadListener.onDownloading(progress);
		}
	}

	public void downComplete(String downloadFilePath) {
		setDownloadFileSize(0);
		setDownloading(false);
		if (mDownloadListener != null) {
			mDownloadListener.onDownloadComplete(downloadFilePath);
		}
	}

	public void downErr() {
		setDownloadFileSize(0);
		setDownloading(false);
		if (mDownloadListener != null) {
			mDownloadListener.onDownloadError();
		}
	}
	
	
	public void downloadFile(final DownloadConnectInfo connectInfo,
			OnDownloadListener downloadListener, final String directory,
			final String downloadFile, final String saveFile) {
		setDownloadListener(downloadListener);
		threadPoolManager.addAsyncTask(new ThreadPoolTask() {
			@Override
			public void run() {
				try {
					boolean connectSuccess = ftpService.getConnect(connectInfo);
					Log.d("downloadFile", "connectSuccess = " + connectSuccess);
					if (connectSuccess) {
						ftpService.download(directory, downloadFile, saveFile);
					} else {
						downErr();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		if (!threadPoolManager.isStart()) {
			threadPoolManager.start();
		}
	}

	public void uploadFile(final DownloadConnectInfo connectInfo,
			final String directory, final String uploadFile) {
		threadPoolManager.addAsyncTask(new ThreadPoolTask() {
			@Override
			public void run() {
				try {
					boolean connectSuccess = ftpService.getConnect(connectInfo);
					if (connectSuccess) {
						ftpService.upload(directory, uploadFile);
					}else {
						downErr();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		if (!threadPoolManager.isStart()) {
			threadPoolManager.start();
		}
	}

	public void deleteFile(final DownloadConnectInfo connectInfo,
			final String directory, final String deleteFile) {
		threadPoolManager.addAsyncTask(new ThreadPoolTask() {
			@Override
			public void run() {
				try {
					boolean connectSuccess = ftpService.getConnect(connectInfo);
					if (connectSuccess) {
						ftpService.delete(directory, deleteFile);
					}else {
						downErr();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		if (!threadPoolManager.isStart()) {
			threadPoolManager.start();
		}
	}

	public void downloadVersionFile(OnDownloadListener downloadListener,DownloadConnectInfo downloadConnectInfo ) {
		Log.d(TAG, "downloadVersionFile");
		setDownloadingApk(false);
		downloadFile(
				new DownloadConnectInfo("222.186.191.157", 21, "wise2012","huaersi"),
				downloadListener, "/wwwroot/wise2014/","MiniMusic.json",
				Environment.getExternalStorageDirectory() + "/wise/");
	}

	public void downloadApk(OnDownloadListener downloadListener) {
		Log.d(TAG, "downloadApk");
		setDownloadingApk(true);
		downloadFile(
				new DownloadConnectInfo("222.186.191.157", 21, "wise2012","huaersi"),
				downloadListener, "/wwwroot/wise2014/","MiniMusic.apk",
				Environment.getExternalStorageDirectory() + "/wise/");
	}
}
