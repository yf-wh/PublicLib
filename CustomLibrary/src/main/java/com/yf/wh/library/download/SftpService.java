package com.yf.wh.library.download;

import android.text.TextUtils;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

public class SftpService implements FileTransfer {

    private static Session session = null;
    private static Channel channel = null;
    private static ChannelSftp sftp = null;
    private SftpDownloadProxy downloadProxy;

    public SftpService() {
    }

    public SftpService(SftpDownloadProxy downloadProxy) {
        this.downloadProxy = downloadProxy;
    }

    public void setDownloadProxy(SftpDownloadProxy downloadProxy) {
        this.downloadProxy = downloadProxy;
    }

    @Override public boolean getConnect(DownloadConnectInfo connectInfo) throws Exception {
        boolean connectSuccess = true;
        JSch jsch = new JSch();
        session = jsch.getSession(connectInfo.getUsername(), connectInfo.getHost(), connectInfo.getPort());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setTimeout(30000);
        session.setPassword(connectInfo.getPassword());
        try {
            session.connect();
        } catch (Exception e) {
            e.printStackTrace();
            if (session.isConnected())
                session.disconnect();
            connectSuccess = false;
        }
        channel = session.openChannel("sftp");
        try {
            channel.connect();
        } catch (Exception e) {
            if (channel.isConnected())
                channel.disconnect();
            connectSuccess = false;
        }
        sftp = (ChannelSftp) channel;
        return connectSuccess;
    }

    public void disConn() throws Exception {
        if (null != sftp) {
            sftp.disconnect();
            sftp.exit();
            sftp = null;
        }
        if (null != channel) {
            channel.disconnect();
            channel = null;
        }
        if (null != session) {
            session.disconnect();
            session = null;
        }
    }

    public void upload(String directory, String uploadFile) throws Exception {
        try {
            if (!directory.isEmpty() && !directory.trim().isEmpty()) {
                try {
                    sftp.cd(directory);
                } catch (SftpException e) {
                    if (ChannelSftp.SSH_FX_NO_SUCH_FILE == e.id) {
                        sftp.mkdir(directory);
                        sftp.cd(directory);
                    }
                }
            }
            File file = new File(uploadFile);
            sftp.put(new FileInputStream(file), file.getName());
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        disConn();
    }


    @Override public void download(String directory, String downloadFile, String saveFile) throws Exception {
        Log.d("SftpService", "download---->directory:" + directory + "\n" +
                "downloadFile:" + downloadFile + "\n" +
                "saveFileDir:" + saveFile);
        try {
            sftp.cd(directory);
            File file = new File(saveFile);
            boolean bFile = file.exists();
            if (!bFile) {
                bFile = file.mkdirs();
            }
            if (bFile) {
                deleteFolderFile(saveFile, false);
                final String filePath = file.getAbsolutePath() + "/" + downloadFile;
                Log.d("SftpService", filePath);
                Log.d("SftpService", "downloadFile start...................");
                OutputStream outs = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
                sftp.get(downloadFile, outs, new SftpProgressMonitor() {
                    int curCount = 0;

                    @Override public void init(int op, String src, String dest, long max) {
                        Log.d("SftpService",
                                "op:" + op + "\n" +
                                "src:" + src + "\n" +
                                "dest:" + dest + "\n" +
                                "max:" + max);
                        downloadProxy.downStart(max);
                    }

                    @Override public boolean count(long count) {
                        curCount += count;
                        Log.d("SftpService", "curCount:" + curCount);
                        downloadProxy.downloading(curCount);
                        return true;
                    }

                    @Override public void end() {
                        downloadProxy.downComplete(filePath);
                        Log.d("SftpService", "end");
                    }
                });
                Log.d("SftpService", "downloadFile complete...................");
            }
        } catch (Exception e) {
            downloadProxy.downErr();
        }
        disConn();
    }

    public void delete(String directory, String deleteFile) throws Exception {
        try {
            sftp.cd(directory);
            sftp.rm(deleteFile);
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
        disConn();
    }

    /**
     * * @param deleteThisPath
     * * @param filepath
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void deleteFolderFile(String filePath, boolean deleteThisPath) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                File file = new File(filePath);
                if (file.isDirectory()) {
                    File files[] = file.listFiles();
                    for (File file1 : files) {
                        deleteFolderFile(file1.getAbsolutePath(), true);
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory()) {
                        file.delete();
                    } else {
                        if (file.listFiles().length == 0) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
