package com.yf.wh.library.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import android.util.Log;

public class FtpService implements FileTransfer {
    private static String TAG = FtpService.class.getSimpleName();

    private static FTPClient ftp = null;
    private static String encoding = "";
    private FtpDownloadProxy downloadManager;

    public FtpService(FtpDownloadProxy downloadManager) {
        this.downloadManager = downloadManager;
    }

    public void setDownloadManager(FtpDownloadProxy downloadManager) {
        this.downloadManager = downloadManager;
    }

    @Override public boolean getConnect(DownloadConnectInfo connectInfo) throws Exception {
        boolean connectSuccess = true;
        try {
            ftp = new FTPClient();
            ftp.setConnectTimeout(30 * 1000);
            ftp.connect(connectInfo.getHost(), connectInfo.getPort());
            ftp.login(connectInfo.getUsername(), connectInfo.getPassword());
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                disConn();
                connectSuccess = false;
            }
            encoding = System.getProperty("file.encoding");
            ftp.setControlEncoding(encoding);
            FTPClientConfig config = new FTPClientConfig(ftp
                    .getSystemType().split(" ")[0]);
            config.setServerLanguageCode("zh");
            ftp.configure(config);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

        } catch (Exception e) {
            System.out.print(e.getMessage() + e);
            connectSuccess = false;
        }
        return connectSuccess;
    }

    @Override
    public void disConn() throws Exception {
        try {
            if (ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
            }
        } catch (Exception e) {
            throw new Exception("disconnect ftp fail");
        }
    }

    @Override
    public void upload(String directory, String uploadFile) throws Exception {
        /*
         * directory = new String(directory.getBytes(encoding),"iso-8859-1");
		 * uploadFile = new String(uploadFile.getBytes(encoding),"iso-8859-1");
		 */
        try {
            if (directory != null && !directory.trim().isEmpty()) {
                String[] pathes = directory.split("/");
                for (String onepath : pathes) {
                    if (onepath == null || onepath.trim().isEmpty()) {
                        continue;
                    }
                    if (!ftp.changeWorkingDirectory(onepath)) {
                        ftp.makeDirectory(onepath);
                        ftp.changeWorkingDirectory(onepath);
                    }
                }
            }
            File file = new File(uploadFile);
            FileInputStream input = new FileInputStream(file);
            ftp.storeFile(file.getName(), input);
            input.close();
        } catch (Exception e) {
            throw new Exception("upload file fail" + e.getMessage());
        }
        disConn();
    }

    @Override
    public void download(String directory, final String downloadFile,
                         String saveFile) throws Exception {
        boolean ret = false;
        String localSaveFilePath = saveFile + "/" + downloadFile;
        try {
            /*
             * directory = new
			 * String(directory.getBytes(encoding),"iso-8859-1"); downloadFile =
			 * new String(downloadFile.getBytes(encoding),"iso-8859-1");
			 * saveFile = new String(saveFile.getBytes(encoding),"iso-8859-1");
			 */
            ftp.changeWorkingDirectory(directory);
            FTPFile[] fs = ftp.listFiles();
            for (FTPFile ff : fs) {
                if (ff.getName().equals(downloadFile)) {
                    final File localFile = new File(saveFile);
                    boolean bFile = localFile.exists();
                    if (!bFile) {
                        bFile = localFile.mkdirs();
                    }
                    if (bFile){
                        OutputStream os = new FileOutputStream(localSaveFilePath);
                        final FTPFile downFile = ff;
                        ftp.setCopyStreamListener(new CopyStreamListener() {

                            boolean isStart = true;
                            @Override
                            public void bytesTransferred(CopyStreamEvent streamEvent) {
                                Log.d("FtpService", "bytesTransferred size = "
                                        + streamEvent.getTotalBytesTransferred());
                            }

                            @Override
                            public void bytesTransferred(long downloadSize,
                                                         int arg1, long arg2) {
                                if (isStart) {
                                    Log.d("FtpService", "download arg0 = "
                                            + downloadSize + "--download arg1 = "
                                            + arg1 + "---download arg2 = " + arg2);
                                    Log.d("FtpService", "downFile size = "
                                            + downFile.getSize());

                                    downloadManager.downStart(downFile.getSize());
                                    isStart = false;
                                }
                                downloadManager.downloading(downloadSize);
                            }
                        });
                        ret = ftp.retrieveFile(ff.getName(), os);
                        Log.d(TAG, "ret = " + ret);
                        os.close();
                    }
                }else {
                    downloadManager.downErr();
                }
            }
        } catch (Exception e) {
            downloadManager.downErr();
            e.printStackTrace();
            // throw new Exception(e);
        }
        disConn();
        if (ret) {
            downloadManager.downComplete(localSaveFilePath);
        }
    }

    @Override
    public void delete(String directory, String deleteFile) throws Exception {
        try {
            /*
			 * directory = new
			 * String(directory.getBytes(encoding),"iso-8859-1"); deleteFile =
			 * new String(deleteFile.getBytes(encoding),"iso-8859-1");
			 */
            // ftp.doCommand("DELE", directory+"/"+deleteFile);
            if (!ftp.deleteFile(directory + "/" + deleteFile)) {
                throw new Exception("delect file fail" + ftp.getReplyString());
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        disConn();
    }
}
