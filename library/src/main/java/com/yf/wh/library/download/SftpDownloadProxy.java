package com.yf.wh.library.download;

import android.os.Environment;

import com.yf.wh.library.download.asynctask.ThreadPoolManager;
import com.yf.wh.library.download.asynctask.ThreadPoolTask;


/**
 * Created by wuhuai on 2016/4/6 .
 * sftp download manager class
 */
public class SftpDownloadProxy {

    private SftpService sftp;
    private static SftpDownloadProxy instance;
    private ThreadPoolManager threadPoolManager;
    private OnDownloadListener downloadListener;

    private long downloadFileSize = 0;
    private boolean isDownloading = false;
    private boolean isDownloadingApk = false;

    private SftpDownloadProxy() {
        sftp = new SftpService(this);
        threadPoolManager = new ThreadPoolManager(ThreadPoolManager.TYPE_FIFO, 5);
    }

    public static synchronized SftpDownloadProxy getInstance() {
        if (instance == null) {
            synchronized (SftpDownloadProxy.class) {
                if (instance == null)
                    instance = new SftpDownloadProxy();
            }
        }
        return instance;
    }

    public void setDownloadListener(OnDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
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

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public boolean isDownloadingApk() {
        return isDownloadingApk;
    }

    public void setDownloadingApk(boolean downloadingApk) {
        isDownloadingApk = downloadingApk;
    }

    public void downStart(long downloadFileSize) {
        setDownloadFileSize(downloadFileSize);
        setDownloading(true);
        if (downloadListener != null) {
            downloadListener.onDownloadStart(downloadFileSize);
        }
    }

    public void downloading(int progress) {
        if (downloadListener != null) {
            downloadListener.onDownloading(progress);
        }
    }

    public void downComplete(String downloadFilePath) {
        setDownloadFileSize(0);
        setDownloading(false);
        if (downloadListener != null) {
            downloadListener.onDownloadComplete(downloadFilePath);
        }
    }

    public void downErr() {
        setDownloadFileSize(0);
        setDownloading(false);
        if (downloadListener != null) {
            downloadListener.onDownloadError();
        }
    }

    public void downFile(final OnDownloadListener downloadListener, final DownloadConnectInfo connectInfo,
                         final String directory, final String downloadFile,final String saveFile) {
        setDownloadListener(downloadListener);
        threadPoolManager.addAsyncTask(new ThreadPoolTask() {
            @Override
            public void run() {
                try {
                    boolean connectSuccess =  sftp.getConnect(connectInfo);
                    if (connectSuccess){
                        sftp.download(directory, downloadFile, saveFile);
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

    public void uploadFile(final DownloadConnectInfo connectInfo, final String directory, final String uploadFile) {
        threadPoolManager.addAsyncTask(new ThreadPoolTask() {
            @Override
            public void run() {
                try {
                    boolean connectSuccess =  sftp.getConnect(connectInfo);
                    if (connectSuccess){
                        sftp.upload(directory, uploadFile);
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

    public void deleteFile(final DownloadConnectInfo connectInfo, final String directory, final String deleteFile) {
        threadPoolManager.addAsyncTask(new ThreadPoolTask() {
            @Override
            public void run() {
                try {
                    boolean connectSuccess =  sftp.getConnect(connectInfo);
                    if (connectSuccess){
                        sftp.delete(directory, deleteFile);
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

    public void downVersionFile(OnDownloadListener downloadListener) {
        setDownloadingApk(false);
        downFile(downloadListener, new DownloadConnectInfo("114.215.198.129", 22, "yfspace", "g4AAkZcuWJOOmh2p"),
                "/home/yfspace/", "SmartTreadmill.json", Environment.getExternalStorageDirectory() + "/taorong/ruizi/");
    }

    public void downApkFile(OnDownloadListener downloadListener) {
        setDownloadingApk(true);
        downFile(downloadListener, new DownloadConnectInfo("114.215.198.129", 22, "yfspace", "g4AAkZcuWJOOmh2p"),
                "/home/yfspace/", "SmartTreadmill.apk", Environment.getExternalStorageDirectory() + "/taorong/ruizi/");
    }

}
