package com.yf.wh.library.download;

/**
 * ftp file transfer interface
 */

public interface FileTransfer {

    /**
     * connect ftp/sftp service
     *
     * @param connectInfo connect service login info
     */
    public abstract boolean getConnect(DownloadConnectInfo connectInfo) throws Exception;

    /**
     * disconnect
     */
    public abstract void disConn() throws Exception;

    /**
     * upload file
     *
     * @param directory the path of the file on the server
     * @param uploadFile upload the file name
     */
    public abstract void upload(String directory, String uploadFile) throws Exception;

    /**
     * download file
     *
     * @param directory the path of the file on the server
     * @param downloadFile under the path of the file name
     * @param saveFile save the download file to the local current path ( not including the file name )
     */
    public abstract void download(String directory, String downloadFile, String saveFile) throws Exception;

    /**
     * delete file
     *
     * @param directory  the path of the file on the server
     * @param deleteFile  the name of the deleted file
     */
    public abstract void delete(String directory, String deleteFile) throws Exception;

}