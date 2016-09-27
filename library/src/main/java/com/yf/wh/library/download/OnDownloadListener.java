package com.yf.wh.library.download;

public interface OnDownloadListener {

        void onDownloadStart(long fileSize);

        void onDownloading(long progress);

        void onDownloadComplete(String filePath);

        void onDownloadError();
}
