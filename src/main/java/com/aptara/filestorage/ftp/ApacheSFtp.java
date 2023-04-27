/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aptara.filestorage.ftp;

/**
 *
 * @author Sushil Sharma
 */
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class ApacheSFtp {

    Session session = null;

    //*** Added by Sushil on 29Nov2019 ***        
    public ChannelSftp sftp = null;
    public String host = "";
    public String user = "";
    public String pass = "";
    // public String clientId = "";

    FTPClient ftp = null;

    public String clientId = "other";

    public ApacheSFtp(String host, String user, String pass, String clientId) throws Exception {

        this.host = host;
        this.user = user;
        this.pass = pass;
        SOP("inside ApacheSFTP : host:-> " + host + " : " + user + " : " + clientId);
        if (host.contains("apollo.aptaracorp.com") || host.contains("ftp.aptaracorp.com")) { // for live
            //if (host.contains("192.168.192.117")) {// for test
            this.clientId = "sftp";
        }

        if (this.clientId.equals("sftp")) {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 22);
            session.setPassword(pass);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");

            session.setConfig(config);
            session.connect();

            if (!session.isConnected()) {
                throw new Exception("Unable to connect to server,Please try again.");
            } else {
                System.out.println("sftp connected..");
            }
            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
        } else {
            ftp = new FTPClient();
            ftp.connect(host);
            int reply;
            reply = ftp.getReplyCode();
            //System.out.println("Reply --> "+reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                try {
                    ftp.disconnect();
                } catch (Exception e) {
                    System.err.println("Unable to disconnect from server after server refused connection. " + e.toString());
                }
                throw new Exception("FTP server refused connection.");
            }
            SOP("Connected to " + host + ". " + ftp.getReplyString());
            if (!ftp.login(user, pass)) {
                throw new Exception("Unable to connect Production Server");
            }
        }
    }

    public boolean disConnect() {
        boolean isDisconnected = false;
        try {
            System.out.println("disConnect");
            if (ftp != null) {
                ftp.disconnect();
                ftp = null;
            }
            if (sftp != null) {
                sftp.disconnect();
                sftp = null;
            }
            if (session != null) {
                session.disconnect();
                session = null;
                isDisconnected = true;
            }
        } catch (Exception caught) {
            caught.printStackTrace();
        }
        return isDisconnected;
    }

    /**
     * This method is called recursively to UploadFileJA To SFTP server
     *
     * @param sourcePath
     * @param destinationPath
     * @throws SftpException Created By : By Niraj
     */
    public String lastModifiedFileName(String productionServerFilePath) throws Exception {
        String oldestFile = null;
        try {

            if (sftp.isConnected()) {
                sftp.cd(productionServerFilePath);

                Vector<LsEntry> vector = (Vector<LsEntry>) sftp.ls(productionServerFilePath);
                String nextName = null;
                LsEntry lsEntry = null;
                int nextTime;
                if (vector != null && !vector.isEmpty()) {

                    ChannelSftp.LsEntry list = vector.get(2);
                    oldestFile = list.getFilename();
                    SftpATTRS attrs = list.getAttrs();
                    int currentOldestTime = attrs.getMTime();

                    //System.out.println("First FileName " + oldestFile + "[" + currentOldestTime + "]");
                    for (Object sftpFile : vector) {

                        lsEntry = (ChannelSftp.LsEntry) sftpFile;
                        nextName = lsEntry.getFilename();
                        attrs = lsEntry.getAttrs();
                        nextTime = attrs.getMTime();

                        if (!lsEntry.getAttrs().isDir()) {
                            //System.out.println("To Compare  " + nextName + "[" + nextTime + "] V/s " + oldestFile + "[" + currentOldestTime + "]");

                            if (!(".").equals(nextName) && !("..").equals(nextName)) {
                                if (nextTime > currentOldestTime) {
                                    oldestFile = nextName;
                                    currentOldestTime = nextTime;
                                    System.out.println("Last Modify Files  : " + oldestFile + "[" + currentOldestTime + "]");
                                }
                            }
                        }
                    }

                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("**********Last Modified File Name : " + oldestFile + " ************");
        return oldestFile;
    }

    //1 *
    public boolean UploadFile(String serverPath, String clientPath, String filetoUpload, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return UploadFile(serverPath, clientPath, filetoUpload);
        }
        SOP("ServerPath ::: " + serverPath);
        SOP("ClientPath ::: " + clientPath);
        SOP("FiletoUpload ::: " + filetoUpload);
        boolean isUploaded = false;
        FileInputStream fis = null;
        File inputFile = new File(clientPath + "/" + filetoUpload);

        if (!inputFile.exists() || !inputFile.isFile()) {
            throw new Exception(filetoUpload + "is not valid file.");
        }

        try {
            fis = new FileInputStream(inputFile);
            if (sftp.isConnected()) {
                sftp.cd(serverPath);
                sftp.put(fis, filetoUpload);
                if (CheckFileExist(serverPath, filetoUpload, clientId)) {
                    isUploaded = true;
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SOP("Exception caught in uploadFile " + ex.getMessage());
        } finally {
            try {
                fis.close();
                disConnect();
            } catch (Exception e) {
                SOP("Problem in closing file" + e);
            }
        }

        return isUploaded;
    }

    //1 *
    public boolean DeleteFile(String serverPath, String filetoDelete, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return DeleteFile(serverPath, filetoDelete);
        }

        boolean fileDeleted = false;
        if (sftp.isConnected()) {
            if (CheckFileExist(serverPath, filetoDelete, clientId)) {
                sftp.cd(serverPath);
                sftp.rm(filetoDelete);
                if (!CheckFileExist(serverPath, filetoDelete, clientId)) {
                    fileDeleted = true;
                }
            } else {
                throw new Exception("Requested does not exits on the server path " + serverPath);
            }
        } else {
            throw new Exception("SFtp server is not connected.");
        }
        return fileDeleted;
    }

    //1 *
    public InputStream getInputStream(String productionServerFilePath, String fileName, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return getInputStream(productionServerFilePath, fileName);
        }

        InputStream iStream = null;
        if (sftp.isConnected()) {
            if (CheckFileExist(productionServerFilePath, fileName, clientId)) {
                sftp.cd(productionServerFilePath);
                iStream = sftp.get(fileName);
            } else {
                throw new Exception("Requested does not exits on the server path " + productionServerFilePath);
            }
        } else {
            throw new Exception("SFtp server is not connected.");
        }
        return iStream;
    }

    //1 *
    public boolean UploadFile(String serverPath, String filetoUpload, InputStream fileUpload, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return UploadFile(serverPath, filetoUpload, fileUpload);
        }

        boolean isUploaded = false;
        try {
            if (sftp.isConnected()) {
                sftp.cd(serverPath);
                sftp.put(fileUpload, filetoUpload);
                if (CheckFileExist(serverPath, filetoUpload, clientId)) {
                    isUploaded = true;
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception ex) {
            throw new Exception("Unable to upload file : " + filetoUpload + " : " + ex.getMessage());
        } finally {
            disConnect();
        }
        return isUploaded;
    }

    //1 *
    public boolean RenameFile(String serverPath, String file2rename, String renamewith, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return RenameFile(serverPath, file2rename, renamewith);
        }

        boolean fileRenamed = false;
        if (sftp.isConnected()) {
            if (CheckFileExist(serverPath, file2rename, clientId)) {
                sftp.cd(serverPath);
                if (serverPath != null && serverPath.trim().length() > 0) {
                    sftp.rename(file2rename, serverPath + "/" + renamewith);
                    if (!CheckFileExist(serverPath, renamewith, clientId)) {
                        fileRenamed = true;
                    }
                } else {
                    sftp.rename(file2rename, renamewith);
                    if (!CheckFileExist(serverPath, renamewith, clientId)) {
                        fileRenamed = true;
                    }
                }
            } else {
                throw new Exception("Requested does not exits on the server path " + serverPath);
            }
        } else {
            throw new Exception("SFtp server is not connected.");
        }
        return fileRenamed;
    }

    //1
    public float getFileSizeFromFtpInKB(String fileName, String path, String clientId) {

        if (!"sftp".equals(this.clientId)) {
            return getFileSizeFromFtpInKB(fileName, path);
        }

        long fileSize = 0;
        SftpATTRS attrs = null;
        try {
            if (sftp.isConnected()) {
                sftp.cd(path);
                ChannelSftp.LsEntry list = (ChannelSftp.LsEntry) sftp.ls(fileName).firstElement();

                attrs = list.getAttrs();
                if (!attrs.isDir()) {
                    if (!(".").equals(list.getFilename()) && !("..").equals(list.getFilename())) {
                        //System.out.println("FileName :"+oListItem.getFilename()); 
                        //arraylist.put(oListItem.getFilename(), attrs.getSize());
                        fileSize = attrs.getSize();
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileSize / (float) 1024;
    }

    //1
    public ArrayList<String> listAll(String ServerPath, String clientId) throws Exception {

        SOP("listAll clientId : " + clientId);
        SOP("host2: " + host + " user: " + user + " pass: " + pass);
        if (!"sftp".equals(this.clientId)) {
            return listAll(ServerPath);
        }

        ArrayList<String> arraylist = null;
        SOP("listAll clientId : " + clientId);
        SOP("host1: " + host + " user: " + user + " pass: " + pass);
        try {
            if (sftp.isConnected()) {
                SOP("host: " + host + " user: " + user + " pass: " + pass);
                sftp.cd(ServerPath);
                SOP("ServerPath :: " + ServerPath);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(ServerPath);
                SOP("ServerPath ::11 ");
                if (list != null && !list.isEmpty()) {
                    arraylist = new ArrayList<String>();
                    for (ChannelSftp.LsEntry oListItem : list) {
                        if (!(".").equals(oListItem.getFilename()) && !("..").equals(oListItem.getFilename())) {
                            SOP("oListItem.getFilename()" + oListItem.getFilename());
                            arraylist.add(oListItem.getFilename());
                        }
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("SFtp server is not connected." + ex.getMessage());
        } finally {
            disConnect();
        }
        return arraylist;
    }

    //1
    public Calendar GetFileTimeStamp(String ServerPath, String FileName4TimeStamp, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return GetFileTimeStamp(ServerPath, FileName4TimeStamp);
        }

        long modificationTime = 0;
        Calendar cal = null;
        try {
            if (sftp.isConnected()) {
                sftp.cd(ServerPath);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(ServerPath);
                if (list != null && !list.isEmpty()) {
                    ArrayList<String> arraylist = new ArrayList<String>();
                    for (ChannelSftp.LsEntry oListItem : list) {
                        if (!oListItem.getAttrs().isDir()) {
                            if (!(".").equals(oListItem.getFilename()) && !("..").equals(oListItem.getFilename())) {
                                System.out.println("FileName :" + oListItem.getFilename());
                                if (FileName4TimeStamp.equals(oListItem.getFilename())) {
                                    arraylist.add(oListItem.getFilename());
                                    modificationTime = sftp.lstat(ServerPath + "/" + FileName4TimeStamp).getMTime() * 1000L;
                                    cal = Calendar.getInstance();
                                    cal.setTimeInMillis(modificationTime);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sftp != null) {
                sftp.disconnect();
            }
        }
        return cal;
    }

    //1
    public String[] listNew(String ServerPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return listNew(ServerPath);
        }

        SOP("ServerPath::" + ServerPath);
        String readNewList[] = null;
        ArrayList arraylist = null;
        try {
            if (sftp.isConnected()) {
                sftp.cd(ServerPath);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(ServerPath);
                if (list != null && !list.isEmpty()) {
                    arraylist = new ArrayList<String>();
                    for (ChannelSftp.LsEntry oListItem : list) {
                        if (!oListItem.getAttrs().isDir()) {
                            if (!(".").equals(oListItem.getFilename()) && !("..").equals(oListItem.getFilename())) {
                                //System.out.println("FileName :"+oListItem.getFilename()); 
                                arraylist.add(oListItem.getFilename());
                            }
                        }
                    }
                    if (arraylist != null) {
                        readNewList = new String[arraylist.size()];
                    }
                    for (int a = 0; a < arraylist.size(); a++) {
                        readNewList[a] = (String) arraylist.get(a);
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } finally {
            disConnect();
        }
        return readNewList;
    }

    //1
    public ArrayList<String> listDirectory(String ServerPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return listDirectory(ServerPath);
        }

        ArrayList<String> arraylist = null;
        try {
            if (sftp.isConnected()) {
                sftp.cd(ServerPath);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(ServerPath);
                if (list != null && !list.isEmpty()) {
                    arraylist = new ArrayList<String>();
                    for (ChannelSftp.LsEntry oListItem : list) {
                        if (oListItem.getAttrs().isDir() && !(".").equals(oListItem.getFilename()) && !("..").equals(oListItem.getFilename())) {
                            SOP("Directory :" + oListItem.getFilename());
                            arraylist.add(oListItem.getFilename());
                        }
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } finally {
            disConnect();
        }
        return arraylist;
    }

    //1
    public boolean DeleteDirectory(String ServerPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return DeleteDirectory(ServerPath);
        }

        boolean fileDeleted = false;
        if (sftp.isConnected()) {
            sftp.cd(ServerPath);
            Vector<ChannelSftp.LsEntry> list = sftp.ls(ServerPath);
            if (list != null && !list.isEmpty()) {
                for (ChannelSftp.LsEntry oListItem : list) {
                    try {
                        if (oListItem.getAttrs().isDir()) {
                            SOP("oListItem.getFilename() is a directory: " + oListItem.getFilename());
                            fileDeleted = deleteDirectory(ServerPath, oListItem.getFilename(), clientId);
                        } else {
                            SOP("oListItem.getFilename() is a file: " + oListItem.getFilename());
                            sftp.rm(ServerPath + "/" + oListItem.getFilename());
                        }
                    } catch (Exception ex) {
                        System.out.println("Unable to delete: " + oListItem.getFilename() + " : " + ex.getMessage());
                    }
                }
            }
            list = sftp.ls(ServerPath);
            if (list != null && list.isEmpty()) {
                fileDeleted = true;
            }
        } else {
            throw new Exception("SFtp server is not connected. " + ServerPath);
        }

        return fileDeleted;
    }

    public boolean deleteDirectory(String productionServerFilePath, String DictoDelete, String clientId) throws Exception {
        boolean fileDeleted = false;
        if ("sftp".equals(this.clientId)) {
            if (sftp.isConnected()) {
                if (CheckFileExist(productionServerFilePath, DictoDelete)) {
                    List<String> files = listAllWithOutDisconnect(productionServerFilePath + "/" + DictoDelete);
                    sftp.cd(productionServerFilePath + "/" + DictoDelete);
                    for (String file : files) {
                        sftp.rm(file);
                    }
                    sftp.cd(productionServerFilePath);
                    sftp.rmdir(DictoDelete);
                    if (!CheckFileExist(productionServerFilePath, DictoDelete)) {
                        fileDeleted = true;
                    }
                } else {
                    throw new Exception("Requested does not exits on the server path " + productionServerFilePath);
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        }
        return fileDeleted;
    }

    public ArrayList listAllWithOutDisconnect(String SFTPWORKINGDIR) throws Exception {
        ArrayList<String> arraylist = null;
        try {
            if (sftp.isConnected()) {
                sftp.cd(SFTPWORKINGDIR);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(SFTPWORKINGDIR);
                if (list != null && !list.isEmpty()) {
                    arraylist = new ArrayList<String>();
                    for (ChannelSftp.LsEntry oListItem : list) {
                        if (!(".").equals(oListItem.getFilename()) && !("..").equals(oListItem.getFilename())) {
                            System.out.println("oListItem.getFilename()" + oListItem.getFilename());
                            arraylist.add(oListItem.getFilename());
                        }
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } finally {
            // disConnect();
        }
        return arraylist;
    }

    //1
    public boolean CreateFolder(String FolderName, String ServerPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return CreateFolder(FolderName, ServerPath);
        }

        boolean isCreated = false;
        if (sftp.isConnected()) {
            if (!CheckFileExist(ServerPath, FolderName, clientId)) {
                sftp.cd(ServerPath);
                sftp.mkdir(FolderName);
                if (CheckFileExist(ServerPath, FolderName, clientId)) {
                    isCreated = true;
                }
            } else {
                isCreated = true;
            }
        } else {
            throw new Exception("SFtp server is not connected.");
        }
        return isCreated;
    }

    //1
    public void mkdirs(String landingPath, String dirTree, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            mkdirs(landingPath, dirTree);
        }

        try {
            boolean dirExists = true;
            if (sftp.isConnected()) {
                sftp.cd(landingPath);
                String[] directories = dirTree.split("/");
                for (String dir : directories) {
                    SOP("dir::" + dir);
                    if (!dir.equals("")) {
                        if (dirExists) {
                            dirExists = CheckFileExist(landingPath, dir);
                        }
                        if (!dirExists) {
                            sftp.cd(landingPath);
                            sftp.mkdir(dir);
                            if (!CheckFileExist(landingPath, dir)) {
                                throw new IOException("Unable to create remote directory '" + dir + "'.");
                            } else {
                                landingPath = landingPath + "/" + dir;
                            }
                        }
                    }
                }
                sftp.cd(landingPath + "/" + dirTree);
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception caught) {
            caught.printStackTrace();
            throw caught;
        } finally {
            disConnect();
        }
    }

    //1
    public boolean setPassiveMode(String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return setPassiveMode();
        }
        return true;
    }

    //1
    public boolean downloadLastModifiedDir(String ServerPath, String ClientPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return downloadLastModifiedDir(ServerPath, ClientPath);
        }

        boolean status = false;
        String lastModifiedFileName = null;
        int currentlatestTime = -1;
        SftpATTRS attrs = null;
        LsEntry lsEntry = null;
        try {
            if (sftp.isConnected()) {
                Vector<LsEntry> vector = (Vector<LsEntry>) sftp.ls(ServerPath);
                System.out.println("Size :" + vector.size());

                for (Object sftpFile : vector) {
                    lsEntry = (ChannelSftp.LsEntry) sftpFile;
                    attrs = lsEntry.getAttrs();
                    System.out.println(lsEntry.getFilename() + "  --  " + attrs.getMTime());
                    if ((!".".equals(lsEntry.getFilename()) && !".rsrc".equals(lsEntry.getFilename()) && !"..".equals(lsEntry.getFilename())) && (attrs.getMTime() > currentlatestTime)) {
                        lastModifiedFileName = lsEntry.getFilename();
                        currentlatestTime = attrs.getMTime();
                    }
                }

                System.out.println("lastModifiedFileName name is ...." + lastModifiedFileName);
                /*   if (lastModifiedFileName.contains(".")) {
                    sftp.get(ServerPath + "/" + lastModifiedFileName, ClientPath);
                    System.out.println("lastModifiedFile :- downloaded successfully");
                    status = true;
                } else {
                    File f = new File(ClientPath + "/" + lastModifiedFileName);
                    if (!f.exists()) {
                        f.mkdir();
                    }
                    downloadFolder(ServerPath, ClientPath, lastModifiedFileName);
                    System.out.println("lastModifiedFile is directory:- downloaded successfully");
                    status = true;
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        disConnect();
        return status;
    }

    //1
    public FTPFile getMaxLastModified(FTPFile[] ftpFiles, String clientId) {

        if (!"sftp".equals(this.clientId)) {
            return getMaxLastModified(ftpFiles);
        }

        return Collections.max(Arrays.asList(ftpFiles), new LastModifiedComparator());
    }

    //1
    public boolean checkPathExists(String ServerPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return checkPathExists(ServerPath);
        }

        boolean fileFind = false;
        SOP("ServerPath=" + ServerPath);
        try {
            //sftp.setFileType(FTP.BINARY_FILE_TYPE);
            sftp.lstat(ServerPath);
            fileFind = true;
        } catch (Exception e) {
            fileFind = false;
            e.printStackTrace();
        }
        //disConnect();
        return fileFind;
    }

    //1
    public boolean downloadFileASCII(String ServerPath, String ClientPath, String FiletoCopy, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return downloadFileASCII(ServerPath, ClientPath, FiletoCopy);
        }

        boolean isDownloaded = false;
        if (sftp.isConnected()) {
            if (CheckFileExist(ServerPath, FiletoCopy)) {
                sftp.cd(ServerPath);
                SOP("downloadFile--> " + FiletoCopy + "\t" + ClientPath);
                sftp.get(FiletoCopy, ClientPath);
                if (checkExistanceOfFiles(ClientPath + File.separator + FiletoCopy)) {
                    isDownloaded = true;
                } else {
                    throw new Exception("Unable to download " + FiletoCopy + " from server path " + ServerPath);
                }
            } else {
                throw new Exception("Requested does not exits on the server path " + ServerPath);
            }
        } else {
            throw new Exception("SFtp server is not connected.");
        }
        return isDownloaded;
    }

    //1
    public boolean downloadFile(String ServerPath, String ClientPath, String FiletoCopy, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return downloadFile(ServerPath, ClientPath, FiletoCopy);
        }

        boolean isDownloaded = false;
        if (sftp.isConnected()) {
            if (CheckFileExist(ServerPath, FiletoCopy, this.clientId)) {
                sftp.cd(ServerPath);
                SOP("downloadFile--> " + FiletoCopy + "\t" + ClientPath);
                if (!new File(ClientPath).exists()) {
                    new File(ClientPath).mkdirs();
                }
                sftp.get(FiletoCopy, ClientPath);

                if (checkExistanceOfFiles(ClientPath + "/" + FiletoCopy)) {
                    isDownloaded = true;
                } else {
                    throw new Exception("Unable to download " + FiletoCopy + " from server path " + ServerPath);
                }
            } else {
                throw new Exception("Requested does not exits on the server path " + ServerPath);
            }
        } else {
            throw new Exception("SFtp server is not connected.");
        }
        return isDownloaded;
    }

    /**
     * ********************** ELD Applet And IO Applet Methods
     * **********************
     */
    //1 
    public boolean downloadDirectory(String parentDir,
            String currentDir, String saveDir, String clientId) throws IOException {
        if (!"sftp".equals(this.clientId)) {
            return downloadDirectory(parentDir, currentDir, saveDir);
        }
        try {
            return downloadFolder(parentDir, saveDir, currentDir, clientId);
        } catch (Exception ex) {
            throw new IOException("Unable to download: " + ex.getMessage());
        }
    }

    public boolean downloadSingleFile(FTPClient ftpClient,
            String remoteFilePath, String savePath, String clientId) throws IOException {

        SOP("inside: downloadSingleFile... ");

        if (!"sftp".equals(this.clientId)) {
            SOP("inside: downloadSingleFile..1. ");
            return downloadSingleFile(ftpClient, remoteFilePath, savePath);
        }

        SOP("inside: downloadSingleFile..2. ");

        String ServerPath = remoteFilePath.substring(0, remoteFilePath.lastIndexOf("/"));
        String ClientPath = savePath.substring(0, savePath.lastIndexOf(File.separator));
        String FiletoCopy = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1, remoteFilePath.length());

        SOP("fileToCopy: " + FiletoCopy);

        try {
            return downloadFile(ServerPath, ClientPath, FiletoCopy, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException("Unabel to download: " + ex.getMessage());
        }
    }

    //1
    public boolean UploadFolder(String ServerPath, String ClientPath, String FolderName, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return UploadFolder(ServerPath, ClientPath, FolderName);
        }

        SOP("ServerPath=" + ServerPath);
        SOP("ClientPath=" + ClientPath);
        SOP("FoldertoUpload=" + FolderName);

        boolean fileUploaded = false;
        try {
            File inputFolder = new File(ClientPath + "/" + FolderName);
            fileUploaded = UploadFolder(inputFolder, ServerPath, clientId);

        } catch (Exception e) {
            System.out.println("The exection in function=" + e);
            throw new Exception("Folder upload fails. Error is -> " + e.getMessage());
        }
        System.out.println("fileUploaded :::::::::: " + fileUploaded);
        return fileUploaded;
    }

    public boolean UploadFolder(File sourceFolder, String DestinationFolder, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return UploadFolder(sourceFolder, DestinationFolder);
        }

        boolean UploadCompleted = false;
        SOP("sourceFolder == " + sourceFolder.getName());
        SOP("DestinationFolder == " + DestinationFolder);
        try {
            sftp.cd(DestinationFolder);
            String FolderName = sourceFolder.getName();
            if (!CheckFileExist(DestinationFolder, FolderName, clientId)) {
                CreateFolder(FolderName, DestinationFolder, clientId);
            }
            DestinationFolder = DestinationFolder + "/" + FolderName;
            if (sourceFolder.isDirectory()) {
                File[] files = sourceFolder.listFiles();
                if (files != null && files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        SOP(i + "--- found:- " + sourceFolder.getAbsolutePath() + "/" + files[i].getName());
                        try {
                            if (files[i].isDirectory()) {
                                UploadFolder(files[i], DestinationFolder, clientId);
                            } else {
                                FileInputStream sourceFileStream = null;
                                sourceFileStream = new FileInputStream(files[i]);
                                sftp.put(sourceFileStream, DestinationFolder + "/" + files[i].getName());
                                sourceFileStream.close();
                                SOP(">>> uploaded:- " + sourceFolder.getAbsolutePath() + "/" + files[i].getName());
                            }
                        } catch (Exception ex) {
                            System.out.println("Unable to upload: " + files[i].getName() + " : " + ex.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("The exection in function=" + e);
            throw new Exception("Folder upload fails. Error is -> " + e.getMessage());
        }
        // Step back up a directory once we're done with the contents of this one.
        try {
            ftp.changeToParentDirectory();
        } catch (IOException e) {
            throw new Exception("IOException caught while attempting to step up to parent directory"
                    + " after successfully processing " + sourceFolder.getAbsolutePath(),
                    e);
        }

        return UploadCompleted;
    }

    //1 *
    public String[] listOnlyFiles(String ServerPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return listOnlyFiles(ServerPath);
        }

        SOP("ServerPath::" + ServerPath);
        String ReadNewList[] = null;
        try {
            if (sftp.isConnected()) {
                sftp.cd(ServerPath);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(ServerPath);
                if (list != null && !list.isEmpty()) {
                    ReadNewList = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        ChannelSftp.LsEntry oListItem = list.get(i);
                        if (!(".").equals(oListItem.getFilename()) && !("..").equals(oListItem.getFilename()) && !oListItem.getAttrs().isDir() && !oListItem.getFilename().startsWith(".")) {
                            SOP("oListItem.getFilename(): " + oListItem.getFilename());
                            ReadNewList[i] = oListItem.getFilename();
                        }
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Exception in listOnlyFiles() : " + ex.getMessage());
        } finally {
            //disConnect();
        }
        return ReadNewList;
    }

    //1 *
    public ArrayList<String> listOnlyDirectory(String ServerPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return listOnlyDirectory(ServerPath);
        }

        ArrayList readLineArray = null;
        SOP("ServerPath::" + ServerPath);
        try {
            if (sftp.isConnected()) {
                sftp.cd(ServerPath);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(ServerPath);
                readLineArray = new ArrayList();
                if (list != null && !list.isEmpty()) {
                    for (int i = 0; i < list.size(); i++) {
                        ChannelSftp.LsEntry oListItem = list.get(i);
                        if (oListItem.getAttrs().isDir() && !oListItem.getFilename().startsWith(".")) {
                            SOP("oListItem.getFilename(): " + oListItem.getFilename());
                            readLineArray.add(oListItem.getFilename());
                        }
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Exception in listOnlyFiles() : " + ex.getMessage());
        } finally {
            //disConnect();
        }
        return readLineArray;
    }

    //1 *
    public boolean UploadFileJA(String FileNameWithPath, String ServerPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return UploadFileJA(FileNameWithPath, ServerPath);
        }

        boolean fileUploaded = false;
        SOP("ServerPath=" + ServerPath);
        SOP("FiletoUploadWithPath=" + FileNameWithPath);

        FileInputStream fis = null;
        File inputFile = new File(FileNameWithPath);

        if (!inputFile.exists() || !inputFile.isFile()) {
            throw new Exception(FileNameWithPath + " not found.");
        }

        try {
            fis = new FileInputStream(inputFile);
            if (sftp.isConnected()) {
                sftp.cd(ServerPath);
                sftp.put(fis, inputFile.getName());
                if (CheckFileExist(ServerPath, inputFile.getName(), clientId)) {
                    fileUploaded = true;
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().indexOf("problem file is") > 0) {
                throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
            }
            SOP("The exection in function=" + e);
        }
        disConnect();
        return fileUploaded;
    }

    /**
     * ********************** LaunchArtApplet.java Methods *******************
     */
//1
    public String[] listAllFilesIncludingDirAlso(String ServerPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return listAllFilesIncludingDirAlso(ServerPath);
        }

        SOP("ServerPath::" + ServerPath);
        String readNewList[] = null;
        ArrayList arraylist = null;
        try {
            if (sftp.isConnected()) {
                sftp.cd(ServerPath);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(ServerPath);
                if (list != null && !list.isEmpty()) {
                    arraylist = new ArrayList<String>();
                    for (ChannelSftp.LsEntry oListItem : list) {
                        if (!(".").equals(oListItem.getFilename()) && !("..").equals(oListItem.getFilename())) {
                            System.out.println("FileName :" + oListItem.getFilename());
                            arraylist.add(oListItem.getFilename());
                        }
                    }
                    SOP("arraylist.size(): " + arraylist.size());
                    if (arraylist != null) {
                        readNewList = new String[arraylist.size()];
                    }
                    for (int a = 0; a < arraylist.size(); a++) {
                        readNewList[a] = (String) arraylist.get(a);
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception in listAllFilesIncludingDirAlso(..): " + ex.getMessage());
        } finally {
            disConnect();
        }
        return readNewList;
    }

    //1
    public boolean checkIfDirectory(String ServerPath, String fName, String clientId) {

        if (!"sftp".equals(this.clientId)) {
            return checkIfDirectory(ServerPath, fName);
        }

        boolean isDirectory = false;
        SOP("ServerPath::" + ServerPath);
        try {
            if (sftp.isConnected()) {
                sftp.cd(ServerPath);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(ServerPath);
                if (list != null && !list.isEmpty()) {
                    for (int i = 0; i < list.size(); i++) {
                        ChannelSftp.LsEntry oListItem = list.get(i);
                        if (oListItem.getAttrs().isDir() && oListItem.getFilename().equals(fName)) {
                            SOP("oListItem.getFilename(): " + oListItem.getFilename());
                            isDirectory = true;
                        }
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception in checkIfDirectory() : " + ex.getMessage());
        } finally {
            //disConnect();
        }
        return isDirectory;
    }

    /**
     * **************************** CTC Methods **************************
     */
    //1
    public boolean UploadFileCTC(String ServerPath, String ClientPath, String FiletoUpload, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return UploadFileCTC(ServerPath, ClientPath, FiletoUpload);
        }

        boolean isUploaded = false;
        FileInputStream fis = null;
        File inputFile = new File(ClientPath + "/" + FiletoUpload);

        if (!inputFile.exists() || !inputFile.isFile()) {
            throw new Exception(FiletoUpload + " not found.");
        }

        try {
            fis = new FileInputStream(inputFile);
            if (sftp.isConnected()) {
                sftp.cd(ServerPath);
                sftp.put(fis, FiletoUpload);
                if (CheckFileExist(ServerPath, FiletoUpload, clientId)) {
                    isUploaded = true;
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } finally {
            try {
                fis.close();
                disConnect();
            } catch (Exception e) {
                SOP("Problem in closing file" + e);
            }
        }

        return isUploaded;
    }

    //1
    public ArrayList getZipFileList(String productionServerFilePath, String ZipFileName, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return getZipFileList(productionServerFilePath, ZipFileName);
        }

        InputStream Istream = null;
        ArrayList fileListInsideZip = new ArrayList();
        try {
            sftp.cd(productionServerFilePath);
            Istream = sftp.get(ZipFileName);
        } catch (Exception ex) {
            System.out.println("Problem in getting Input Stream :::" + ex.getMessage());
        }

        try {
            if (Istream != null) {
                ZipInputStream zipInStream = new ZipInputStream(Istream);
                ZipEntry zipentry = null;
                while ((zipentry = zipInStream.getNextEntry()) != null) {
                    String FileNmaer = zipentry.getName();
                    SOP("FileNmaer=========" + FileNmaer);
                    fileListInsideZip.add(FileNmaer);
                    zipInStream.closeEntry();
                }
                zipInStream.close();
            }
            Istream.close();
            //sftp.completePendingCommand();
        } catch (Exception ex) {
            System.out.println("Problem in getting Input Stream :::" + ex.getMessage());
        }
        return fileListInsideZip;
    }

    //1
    public String[] listFileCTC(String ServerPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return listFileCTC(ServerPath);
        }

        SOP("ServerPath::" + ServerPath);
        String ReadNewList[] = null;
        try {
            if (sftp.isConnected()) {
                sftp.cd(ServerPath);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(ServerPath);
                if (list != null && !list.isEmpty()) {
                    ReadNewList = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        ChannelSftp.LsEntry oListItem = list.get(i);
                        if (!(".").equals(oListItem.getFilename()) && !("..").equals(oListItem.getFilename())) {
                            SOP("oListItem.getFilename(): " + oListItem.getFilename());
                            ReadNewList[i] = oListItem.getFilename();
                        }
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } finally {
            disConnect();
        }
        return ReadNewList;
    }

    /**
     * *************** From ConnectSFTP *************
     */
    //11
    public boolean downloadFolder(String ServerPath, String clientPath, String FoldertoDownload, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return downloadFolder(ServerPath, clientPath, FoldertoDownload);
        }

        String TempServerDirPath = ServerPath;
        String TempclientDirPath = clientPath;

        SOP("downloadFolder :: " + FoldertoDownload);
        SOP("ServerPath :: " + ServerPath);
        SOP("clientPath :: " + clientPath);

        boolean downloaded = false;

        try {
            downloaded = DownloadFolderDetails(ServerPath, clientPath, FoldertoDownload, clientId);
        } catch (Exception ex) {
            System.out.println("Exception in DownloadFolder: " + ex.getMessage());
            //throw new Exception(ex.getMessage());
        }
        clientPath = TempclientDirPath;
        ServerPath = TempServerDirPath;

        return downloaded;
    }

    //22
    public boolean DownloadFolderDetails(String ServerPath, String clientPath, String FoldertoDownload, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return downloadFolder(ServerPath, clientPath, FoldertoDownload);
        }

        SOP("downloadFolder222 :: " + FoldertoDownload);
        SOP("ServerPath222 :: " + ServerPath);
        SOP("clientPath222 :: " + clientPath);
        boolean downloaded = false;
        if (FoldertoDownload != null && !FoldertoDownload.equalsIgnoreCase("")) {
            ServerPath = ServerPath + "/" + FoldertoDownload;
            clientPath = clientPath + "/" + FoldertoDownload;
        }

        String FileSeq = "";
        try {
            ArrayList StageFiles;
            StageFiles = listAllforFolder(ServerPath, clientId);

            /*if(StageFiles.size() > 0)
             {*/
            SOP("StageFiles.size()===" + StageFiles.size());

            if (!new File(clientPath).exists()) {
                new File(clientPath).mkdirs();
            }
            //}
            //SOP("StageFiles.length==="+StageFiles.length);
            if (StageFiles.size() > 0) {
                for (int a = 0; a < StageFiles.size(); a++) {
                    SOP("StageFiles.length===" + StageFiles.size());
                    if (StageFiles.get(a) == null) {
                        continue;
                    }

                    FileSeq = StageFiles.get(a) + "";

                    if (!FileSeq.equalsIgnoreCase(".rsrc")) {
                        if (!FileSeq.startsWith(".") && FileSeq.indexOf(".") <= 0) {
                            DownloadFolderDetails(ServerPath, clientPath, FileSeq, clientId);
                        } else {
                            File outPutFile = new File(clientPath + "/" + FileSeq);
                            if (outPutFile.exists()) {
                                if (outPutFile.delete()) {
                                    SOP("Deleted file :: " + outPutFile.getName());
                                } else {
                                    SOP("Not able to deleted file :: " + outPutFile.getName());
                                }
                            }
                            outPutFile = null;
                            downloadFileForFolder(ServerPath, clientPath, FileSeq, clientId);
                        }
                    }
                }
                downloaded = true;
            } else {
                downloaded = true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SOP("Exception in DownloadFolder recursion: " + ex.getMessage());
            downloaded = false;
            //throw new Exception(ex.getMessage());
        }
        return downloaded;
    }

    //33
    public ArrayList listAllforFolder(String ServerPath, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return listAllforFolder(ServerPath);
        }

        ArrayList readLineArray = null;
        int i = 0;
        try {
            if (sftp.isConnected()) {
                sftp.cd(ServerPath);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(ServerPath);
                if (list != null && !list.isEmpty()) {
                    readLineArray = new ArrayList<String>();
                    for (ChannelSftp.LsEntry oListItem : list) {
                        if (!(".").equals(oListItem.getFilename()) && !("..").equals(oListItem.getFilename())) {
                            SOP("Files :" + oListItem.getFilename());
                            readLineArray.add(oListItem.getFilename());
                        }
                    }
                }
            } else {
                throw new Exception("SFtp server is not connected.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        }
        sftp = null;
        //SOP("in listAllforFolder printWorkingDirectory----" + sftp.printWorkingDirectory());
        return readLineArray;
    }

    //44
    public boolean downloadFileForFolder(String ServerPath, String ClientPath, String FiletoCopy, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return downloadFileForFolder(ServerPath, ClientPath, FiletoCopy);
        }

        boolean isDownloaded = false;
        if (sftp.isConnected()) {
            if (CheckFileExist(ServerPath, FiletoCopy, clientId)) {
                sftp.cd(ServerPath);
                System.out.println("downloadFile--> " + FiletoCopy + "\t" + ClientPath);
                sftp.get(FiletoCopy, ClientPath);
                if (checkExistanceOfFiles(ClientPath + File.separator + FiletoCopy)) {
                    isDownloaded = true;
                } else {
                    throw new Exception("Unable to download " + FiletoCopy + " from server path " + ServerPath);
                }
            } else {
                throw new Exception("Requested does not exits on the server path " + ServerPath);
            }
        } else {
            throw new Exception("SFtp server is not connected.");
        }
        return isDownloaded;

    }

    //11
    public boolean CheckFileExist(String productionServerFilePath, String creteria, String clientId) throws Exception {

        if (!"sftp".equals(this.clientId)) {
            return CheckFileExist(productionServerFilePath, creteria);
        }

        boolean isValidated = false;
        if (sftp.isConnected()) {
            try {
                sftp.cd(productionServerFilePath);
                Vector<ChannelSftp.LsEntry> list = sftp.ls(creteria);
                if (list != null && !list.isEmpty()) {
                    isValidated = true;
                }
            } catch (Exception caught) {
            }
        } else {
            throw new Exception("SFtp server is not connected.");
        }
        //System.out.println("isValidated-->"+isValidated);
        return isValidated;
    }

    public boolean checkExistanceOfFiles(String filePath) {
        boolean status = true;
        System.out.println("filePath---->" + filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            status = false;
        }
        return status;
    }

    /**
     * ********************* Old methods with FTPClient ***********************
     */
    /**
     * **************************** old CTC Methods **************************
     */
    //11
    public boolean UploadFileCTC(String ServerPath, String ClientPath, String FiletoUpload) throws Exception {
        SOP("ServerPath=" + ServerPath);
        SOP("ClientPath=" + ClientPath);
        SOP("FiletoUpload=" + FiletoUpload);
        boolean fileUploaded = false;
        SOP("yyyy---------" + ftp.isConnected());
        FileInputStream fis = null;
        File inputFile = new File(ClientPath + "/" + FiletoUpload);
        if (!inputFile.exists() || !inputFile.isFile()) {
            throw new Exception(FiletoUpload + " not found.");
        }

        try {
            fis = new FileInputStream(inputFile);
            SOP("input list files is " + inputFile.getName());

            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            SOP("ftp.getStatus() :: " + ftp.getStatus());

            ftp.changeWorkingDirectory(ServerPath);

            //SOP(ftp.getReplyString());
            //SOP("Remote system is " + ftp.getSystemName());
            SOP("ftp.getStatus() :: " + ftp.getStatus());
            SOP("ftp.printWorkingDirectory() :: " + ftp.printWorkingDirectory());

            boolean uplValue = false;

            uplValue = ftp.storeFile(inputFile.getName(), fis);
            fileUploaded = uplValue;
            if (!uplValue) {
                SOP("Unable to upload file " + inputFile.getName());
                throw new Exception("Unable to upload file " + inputFile.getParent() + " failed. ftp.storeFile() returned false.");
            }
            if (fis != null) {
                fis.close();
            }
            inputFile = null;
            ///
            // Check ZIP  and compare file size...
            int CheckResult = 0;
            if (FiletoUpload.indexOf(".tar") > 0 || FiletoUpload.indexOf(".zip") > 0 || FiletoUpload.indexOf(".sit") > 0 || FiletoUpload.indexOf(".sitx") > 0) {
                //CheckResult=CompareFiles.check(ClientPath+"/"+FiletoUpload, ftp.retrieveFileStream(FiletoUpload));	//For PC
            }
            ftp.changeToParentDirectory();
            if (CheckResult == 1) {
                throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
            }
        } catch (Exception e) {
            if (e.getMessage().indexOf("problem file is") > 0) {
                throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
            }
            SOP("The exection in function=" + e);
        }
        //disConnect();
        return fileUploaded;
    }

    public ArrayList getZipFileList(String productionServerFilePath, String ZipFileName) throws Exception {

        InputStream Istream = null;
        ArrayList fileListInsideZip = new ArrayList();
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            ftp.changeWorkingDirectory(productionServerFilePath);

            SOP("printWorkingDirectory--after--->" + ftp.printWorkingDirectory());
            Istream = ftp.retrieveFileStream(ZipFileName);

        } catch (Exception ex) {
            System.out.println("Problem in getting Input Stream :::" + ex.getMessage());
        }

        try {
            if (Istream != null) {
                ZipInputStream zipInStream = new ZipInputStream(Istream);
                ZipEntry zipentry = null;
                while ((zipentry = zipInStream.getNextEntry()) != null) {
                    String FileNmaer = zipentry.getName();
                    SOP("FileNmaer=========" + FileNmaer);
                    fileListInsideZip.add(FileNmaer);
                    zipInStream.closeEntry();
                }
                zipInStream.close();
            }
            Istream.close();
            ftp.completePendingCommand();
        } catch (Exception ex) {
            System.out.println("Problem in getting Input Stream :::" + ex.getMessage());
        }
        return fileListInsideZip;
    }

    //1
    public String[] listFileCTC(String ServerPath) throws Exception {

        String ReadNewList[] = null;
        ArrayList readLineArray = null;
        int i = 0;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }

            readLineArray = new ArrayList();

            for (int j = 0; j < size; j++) {
                if (ftpFiles[j].isFile()) {
                    readLineArray.add(ftpFiles[j].getName() + "");
                    SOP("file name in ftp :: " + ftpFiles[j].getName() + "");
                }
            }

            ReadNewList = new String[readLineArray.size()];
            for (int j = 0; j < readLineArray.size(); j++) {
                ReadNewList[j] = readLineArray.get(j) + "";
            }
            //
        } catch (Exception ex) {
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        }
        return ReadNewList;
    }

    /**
     * **************************************************************
     */
    //11
    public boolean CheckFileExist(String ServerPath, String File2Serch) throws Exception {
        boolean fileFind = false;
        SOP("ServerPath=" + ServerPath);
        SOP("File2Serch=" + File2Serch);
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(ServerPath);

            FTPFile[] ftpFiles = ftp.listFiles();

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            SOP("Array size is" + size);
            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            for (int i = 0; i < size; i++) {
                ftpfile = ftpFiles[i];
                String TempFileName = ftpFiles[i].getName();
                if (TempFileName.equals(File2Serch)) {
                    fileFind = true;
                    break;
                }
            }
        } catch (Exception e) {
            fileFind = false;
            if (e.getMessage() != null && e.getMessage().indexOf("problem file is") > 0) {
                throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
            }
            SOP("The exection in function=" + e);
        }
        disConnect();
        return fileFind;
    }

    //11
    public boolean downloadFolder(String ServerPath, String clientPath, String FoldertoDownload) throws Exception {
        String TempServerDirPath = ServerPath;
        String TempclientDirPath = clientPath;
        SOP("downloadFolder :: " + FoldertoDownload);
        SOP("ServerPath :: " + ServerPath);
        SOP("clientPath :: " + clientPath);
        boolean downloaded = false;

        try {
            downloaded = DownloadFolderDetails(ServerPath, clientPath, FoldertoDownload);
        } catch (Exception ex) {
            System.out.println("Exception in DownloadFolder: " + ex.getMessage());
            //throw new Exception(ex.getMessage());
        }
        clientPath = TempclientDirPath;
        ServerPath = TempServerDirPath;

        disConnect();
        return downloaded;

    }

    //11
    public boolean DownloadFolderDetails(String ServerPath, String clientPath, String FoldertoDownload) throws Exception {
        SOP("downloadFolder222 :: " + FoldertoDownload);
        SOP("ServerPath222 :: " + ServerPath);
        SOP("clientPath222 :: " + clientPath);
        boolean downloaded = false;
        if (FoldertoDownload != null && !FoldertoDownload.equalsIgnoreCase("")) {
            ServerPath = ServerPath + "/" + FoldertoDownload;
            clientPath = clientPath + "/" + FoldertoDownload;
        }

        String FileSeq = "";
        try {
            ArrayList StageFiles;
            StageFiles = listAllforFolder(ServerPath);

            if (StageFiles.size() > 0) {
                SOP("StageFiles.size()===" + StageFiles.size());

                if (!new File(clientPath).exists()) {
                    new File(clientPath).mkdirs();
                }
            }
            //SOP("StageFiles.length==="+StageFiles.length);
            if (StageFiles.size() > 0) {
                for (int a = 0; a < StageFiles.size(); a++) {
                    if (StageFiles.get(a) == null) {
                        continue;
                    }

                    FileSeq = StageFiles.get(a) + "";

                    //change by kavita for versions folder
                    if (!FileSeq.equalsIgnoreCase("versions")) {
                        if (FileSeq.indexOf(".") <= 0) {
                            DownloadFolderDetails(ServerPath, clientPath, FileSeq);
                        } else {
                            File outPutFile = new File(clientPath + "/" + FileSeq);
                            if (outPutFile.exists()) {
                                if (outPutFile.delete()) {
                                    SOP("Deleted file :: " + outPutFile.getName());
                                } else {
                                    SOP("Not able to deleted file :: " + outPutFile.getName());
                                }
                            }
                            outPutFile = null;
                            downloadFileForFolder(ServerPath, clientPath, FileSeq);
                        }
                    }
                }///For loop ends
                downloaded = true;
            }
        } catch (Exception ex) {
            SOP("Exception in DownloadFolder recursion: " + ex.getMessage());
            downloaded = false;
            //throw new Exception(ex.getMessage());
        }
        return downloaded;
    }

    //11
    public ArrayList listAllforFolder(String ServerPath) throws Exception {
        ArrayList readLineArray = null;
        int i = 0;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;
            SOP("ServerPath is " + ServerPath);
            SOP("ftpFiles.length " + ftpFiles.length);
            SOP("size is " + size);
            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }

            readLineArray = new ArrayList();

            for (int j = 0; j < size; j++) {
                readLineArray.add(ftpFiles[j].getName() + "");
            }

            //
        } catch (Exception ex) {
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        }
        SOP("in listAllforFolder printWorkingDirectory----" + ftp.printWorkingDirectory());
        return readLineArray;
    }

    //11
    public boolean downloadFileForFolder(String ServerPath, String ClientPath, String FiletoCopy) throws Exception {
        SOP("ServerPath=" + ServerPath);
        SOP("ClientPath=" + ClientPath);
        SOP("FiletoCopy=" + FiletoCopy);
        boolean fileDownloaded = false;

        String inputFile = FiletoCopy;
        File outputFile = new File(ClientPath + "/" + FiletoCopy);
        FileOutputStream fos = null;
        int flag = 0;
        try {
            String inputfiles = inputFile;
            SOP("input list files is " + inputFile);

            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            SOP(ftp.getReplyString());
            SOP("Remote system is " + ftp.getSystemName());

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }
            //SOP("ftpFiles.LENGTH=========== "+ftpFiles.length);
            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            SOP("Array size is  " + size);

            boolean FileFound = false;
            for (int i = 0; i < size; i++) {
                ftpfile = ftpFiles[i];
                String TempFileName = ftpFiles[i].getName();
                if (TempFileName.equals(inputFile)) {
                    FileFound = true;
                    SOP("FileFound :::" + TempFileName);
                    break;
                }
            }
            if (!FileFound) {
                throw new Exception(inputFile + " not exists on server.");
            }

            if (ftpFiles == null || size < 1) {
                throw new Exception("FTP connection closed");
            }

            try {
                boolean retValue = false;
                SOP("file name is " + ftpfile.getName());
                try {
                    fos = new FileOutputStream(outputFile);
                    retValue = ftp.retrieveFile(inputFile, fos);
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    SOP("Exception in downloading file ===>" + e);
                }
                fileDownloaded = retValue;

                if (!retValue) {
                    throw new Exception("Downloading of remote file " + inputFile + " failed. " + ftp.isConnected());
                }
            } catch (Exception e) {
                SOP("The exection in function=" + e);
            }
        } catch (Exception exe) {
            SOP("The exection in function=" + exe);
        }
        SOP("Download completed for -- " + FiletoCopy);
        return fileDownloaded;
    }

    /**
     * **********************************************
     */
    //11
    public boolean UploadFile(String ServerPath, String ClientPath, String FiletoUpload) throws Exception {

        boolean fileUploaded = false;
        SOP("ServerPath=" + ServerPath);
        SOP("ClientPath=" + ClientPath);
        SOP("FiletoUpload=" + FiletoUpload);

        FileInputStream fis = null;
        File inputFile = new File(ClientPath + "/" + FiletoUpload);

        if (!inputFile.exists() || !inputFile.isFile()) {
            throw new Exception(FiletoUpload + " not found.");
        }

        try {
            fis = new FileInputStream(inputFile);

            SOP("input list files is " + inputFile.getName());

            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(ServerPath);

            boolean uplValue = false;

            uplValue = ftp.storeFile(inputFile.getName(), fis);
            fileUploaded = uplValue;

            if (!uplValue) {
                throw new Exception("Unable to upload file " + inputFile.getParent() + " failed. ftp.storeFile() returned false.");
            }
            ///
            // Check ZIP  and compare file size...
            int CheckResult = 0;
            if (FiletoUpload.indexOf(".tar") > 0 || FiletoUpload.indexOf(".zip") > 0 || FiletoUpload.indexOf(".sit") > 0 || FiletoUpload.indexOf(".sitx") > 0 || FiletoUpload.indexOf(".eps") > 0) {
                //CheckResult=CompareFiles.check(ClientPath+"/"+FiletoUpload, ftp.retrieveFileStream(FiletoUpload));	//For PC
            }
            if (CheckResult == 1) {
                throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
            }
            ///
            ///
        } catch (Exception e) {
            if (e.getMessage().indexOf("problem file is") > 0) {
                throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
            }
            SOP("The exection in function=" + e);
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
                SOP("Problem in closing file" + e);
            }
        }
        disConnect();
        return fileUploaded;
    }

    //11
    public boolean DeleteFile(String ServerPath, String FiletoDelete) throws Exception {
        boolean FileDeleted = false;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }

            for (int j = 0; j < size; j++) {
                if (ftpFiles[j].isFile()) {
                    String tempFile = ftpFiles[j].getName() + "";
                    if (tempFile.equals(FiletoDelete)) {
                        FileDeleted = ftp.deleteFile(FiletoDelete);
                        SOP("Inside file deletion method :: " + FileDeleted);
                    }
                }
            }
        } catch (Exception ex) {
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());

        } finally {
            disConnect();
        }
        return FileDeleted;
    }

    //11
    public InputStream getInputStream(String productionServerFilePath, String FileName) throws Exception {
        InputStream Istream = null;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(productionServerFilePath);

            Istream = ftp.retrieveFileStream(FileName);
        } catch (Exception ex) {
            SOP("Problem in getting Input Stream :::" + ex.getMessage());
            throw ex;

        }
        return Istream;
    }

    //11
    public boolean UploadFile(String ServerPath, String FiletoUpload, InputStream FileUpload) throws Exception {
        boolean fileUploaded = false;
        SOP("ServerPath=" + ServerPath);
        SOP("FiletoUpload=" + FiletoUpload);
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(ServerPath);

            boolean uplValue = false;

            uplValue = ftp.storeFile(FiletoUpload, FileUpload);
            fileUploaded = uplValue;

            if (!uplValue) {
                throw new Exception("Unable to upload file " + FiletoUpload + " failed. ftp.storeFile() returned false.");
            }

            int CheckResult = 0;
            if (FiletoUpload.indexOf(".tar") > 0 || FiletoUpload.indexOf(".zip") > 0 || FiletoUpload.indexOf(".sit") > 0 || FiletoUpload.indexOf(".sitx") > 0 || FiletoUpload.indexOf(".eps") > 0) {
                //CheckResult=CompareFiles.check(ClientPath+"/"+FiletoUpload, ftp.retrieveFileStream(FiletoUpload));	//For PC
            }
            if (CheckResult == 1) {
                throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
            }

        } catch (Exception e) {
            if (e.getMessage().indexOf("problem file is") > 0) {
                throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
            }
            SOP("The exection in function=" + e);
        }
        disConnect();
        return fileUploaded;
    }

    //11
    public boolean RenameFile(String ServerPath, String File2rename, String Renamewith) throws Exception {
        boolean Renamed = false;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }

            for (int j = 0; j < size; j++) {
                if (ftpFiles[j].isFile()) {
                    String tempFile = ftpFiles[j].getName() + "";
                    if (tempFile.equals(File2rename)) {
                        Renamed = ftp.rename(File2rename, Renamewith);
                        SOP("Inside file deletion method :: " + Renamed);
                    }
                }
            }
        } catch (Exception ex) {
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        } finally {
            disConnect();
        }
        return Renamed;
    }

    //11
    public float getFileSizeFromFtpInKB(String fileName, String path) {
        long size = 0;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(path);

            boolean changeDir = ftp.changeWorkingDirectory(path);

            FTPFile[] ftpFiles = null;

            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + path);
            }

            for (FTPFile ftpFile : ftpFiles) {
                if (ftpFile.getName().equals(fileName)) {
                    size = ftpFile.getSize();
                    break;
                }
            }
            ftpFiles = null;
            if (ftp != null) {
                ftp.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size / (float) 1024;
    }

    //11
    public ArrayList<String> listAll(String ServerPath) throws Exception {
        ArrayList<String> readLineArray = null;
        int i = 0;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }

            readLineArray = new ArrayList<String>();

            for (int j = 0; j < size; j++) {
                readLineArray.add(ftpFiles[j].getName() + "");
            }
        } catch (Exception ex) {
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        } finally {
            disConnect();
        }
        return readLineArray;
    }

    //11
    public Calendar GetFileTimeStamp(String ServerPath, String FileName4TimeStamp) throws Exception {
        Calendar FileTimeStamp = null;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }

            for (int j = 0; j < size; j++) {
                if (ftpFiles[j].isFile()) {
                    String tempFile = ftpFiles[j].getName() + "";
                    if (tempFile.equals(FileName4TimeStamp)) {
                        FileTimeStamp = ftpFiles[j].getTimestamp();
                    }
                }
            }
        } catch (Exception ex) {
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());

        } finally {
            //disConnect();
        }
        return FileTimeStamp;
    }

    //11
    public String[] listNew(String ServerPath) throws Exception {
        SOP("ServerPath::" + ServerPath);
        String ReadNewList[] = null;
        ArrayList<String> readLineArray = null;
        int i = 0;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);

            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }

            readLineArray = new ArrayList<String>();

            for (int j = 0; j < size; j++) {
                SOP("ftpFiles" + j + "==" + ftpFiles[j]);
                if (ftpFiles[j].isFile()) {
                    readLineArray.add(ftpFiles[j].getName() + "");
                }
            }

            ReadNewList = new String[readLineArray.size()];
            for (int j = 0; j < readLineArray.size(); j++) {
                ReadNewList[j] = readLineArray.get(j) + "";
            }
        } catch (Exception ex) {
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        } finally {
            disConnect();
        }
        return ReadNewList;
    }

    //11
    public ArrayList<String> listDirectory(String ServerPath) throws Exception {
        ArrayList<String> readLineArray = null;
        //int i = 0;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }

            readLineArray = new ArrayList<String>();

            for (int j = 0; j < size; j++) {
                if (ftpFiles[j].isDirectory()) {
                    readLineArray.add(ftpFiles[j].getName());
                }
            }
        } catch (Exception ex) {
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());

        } finally {
            disConnect();
        }
        return readLineArray;
    }

    //11
    public boolean DeleteDirectory(String ServerPath) throws Exception {
        boolean FileDeleted = false;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }
            for (int j = 0; j < size; j++) {
                if (ftpFiles[j].isDirectory()) {
                    SOP("Directory ::  " + ServerPath + "/" + ftpFiles[j].getName());
                    SOP("Group ::  " + ftpFiles[j].getGroup());
                    SOP("Listing ::  " + ftpFiles[j].getRawListing());
                    DeleteDirectory(ServerPath + "/" + ftpFiles[j].getName() + "");
                } else {
                    SOP("Directory ::  " + ServerPath + "/" + ftpFiles[j].getName());
                    SOP("Group ::  " + ftpFiles[j].getGroup());
                    SOP("Listing ::  " + ftpFiles[j].getRawListing());
                    String tempFile = ftpFiles[j].getName();
                    FileDeleted = ftp.deleteFile(tempFile);
                    SOP("Inside file deletion method :: " + FileDeleted);
                }
            }

            String folder = ServerPath.substring(ServerPath.lastIndexOf("/") + 1);
            String ServerPath1 = ServerPath.substring(0, ServerPath.lastIndexOf("/"));

            //ftp.changeWorkingDirectory(ServerPath1);
            //FTPFile[] ftpFiles1 = ftp.listFiles();2222222
            boolean changeDir1 = ftp.changeWorkingDirectory(ServerPath1);
            FTPFile[] ftpFiles1 = null;
            if (changeDir1) {
                ftpFiles1 = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath1);
            }

            int size1 = (ftpFiles1 == null) ? 0 : ftpFiles1.length;
            for (int j = 0; j < size1; j++) {
                if (ftpFiles1[j].isDirectory()) {
                    String tempDir = ftpFiles1[j].getName();
                    if (tempDir.equals(folder)) {
                        FileDeleted = ftp.removeDirectory(ServerPath);
                        SOP("Inside file directory deletion method :: " + FileDeleted);
                    }
                }
            }
        } catch (Exception ex) {
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        }
        return FileDeleted;
    }

    //11
    public boolean CreateFolder(String FolderName, String ServerPath) throws Exception {
        boolean FolderCreated = false;
        SOP("FolderName == " + FolderName);
        SOP("ServerPath == " + ServerPath);
        try {
            ftp.changeWorkingDirectory(ServerPath);
            if (!ftp.changeWorkingDirectory(FolderName)) {
                ftp.makeDirectory(FolderName);
                if (ftp.changeWorkingDirectory(FolderName)) {
                    FolderCreated = true;
                }
            }
        } catch (Exception e) {
            System.out.println("The exection in folder creation=" + e);
            throw new Exception("Folder creation fails. Error is -> " + e.getMessage());
        }

        return FolderCreated;
    }

    //11
    public void mkdirs(String landingPath, String dirTree) throws Exception {
        try {
            boolean dirExists = true;
            boolean returnedRes = true;
            ftp.changeWorkingDirectory(landingPath);

            String[] directories = dirTree.split("/");
            for (String dir : directories) {
                SOP("dir::" + dir);
                if (!dir.equals("")) {
                    if (dirExists) {
                        dirExists = ftp.changeWorkingDirectory(dir);
                    }
                    if (!dirExists) {
                        if (!ftp.makeDirectory(dir)) {
                            throw new IOException("Unable to create remote directory '" + dir + "'.  error='" + ftp.getReplyString() + "'");
                        }
                        if (!ftp.changeWorkingDirectory(dir)) {

                            throw new IOException("Unable to change into newly created remote directory '" + dir + "'.  error='" + ftp.getReplyString() + "'");
                        }
                    }
                }
            }
        } catch (Exception caught) {
            caught.printStackTrace();
            throw caught;
        } finally {
            disConnect();
        }
    }

    //11
    public boolean setPassiveMode() throws Exception {
        boolean SetPassive = false;

        ftp.enterLocalPassiveMode();
        SetPassive = true;

        return SetPassive;
    }

    //11
    public boolean downloadLastModifiedDir(String ServerPath, String ClientPath) throws Exception {
        SOP("--------ServerPath--------" + ServerPath);
        SOP("--------ClientPath--------" + ClientPath);

        boolean fileDownloaded = false;
        FileOutputStream outputStream = null;
        String downloadFileName = "";
        try {

            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.getReplyString();
            ftp.getSystemName();

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpAllFiles = null;
            if (changeDir) {
                ftpAllFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            if (ftpAllFiles.length == 0) {
                throw new Exception("No file Found! On - " + ServerPath);
            }

            ftpfile = getMaxLastModified(ftpAllFiles);
            SOP("---file on ftp---" + ftpfile);
            downloadFileName = ftpfile.getName();
            if (downloadFileName == null) {
                throw new Exception("----download FileName not exists on server.");
            }

            SOP("----Download zip name----------" + downloadFileName);;

            File outputFile = new File(ClientPath + "/" + downloadFileName);

            try {
                outputStream = new FileOutputStream(outputFile);
                boolean retValue = false;
                retValue = ftp.retrieveFile(downloadFileName, outputStream);
                fileDownloaded = retValue;
                if (!retValue) {
                    throw new Exception("Downloading file " + downloadFileName + " failed. ftp.retrieveFile() returned false.");
                }
            } catch (Exception e) {
                SOP("Error in function---" + e);
                throw new Exception("Downloading file " + downloadFileName + " failed. ");
            }
        } catch (Exception exe) {
            SOP("The exection in function=" + exe);
            if (exe != null && exe.getMessage().contains("not exists on server")) {
                throw new Exception("No file to download.");
            } else {
                throw new Exception("Downloading of remote file " + downloadFileName + " failed. ");
            }
        }
        if (outputStream != null) {
            outputStream.close();
        }
        disConnect();
        return fileDownloaded;
    }

    //11
    public FTPFile getMaxLastModified(FTPFile[] ftpFiles) {
        return Collections.max(Arrays.asList(ftpFiles), new LastModifiedComparator());
    }

    //11
    public boolean checkPathExists(String ServerPath) throws Exception {
        boolean folderAvailable = false;

        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            if (changeDir) {
                folderAvailable = true;
            } else {
                folderAvailable = false;
            }
        } catch (Exception ex) {
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        } finally {
            disConnect();
        }
        return folderAvailable;
    }

    //11
    public boolean downloadFileASCII(String ServerPath, String ClientPath, String FiletoCopy) throws Exception {
        SOP("ServerPath=" + ServerPath);
        SOP("ClientPath=" + ClientPath);
        SOP("FiletoCopy=" + FiletoCopy);
        boolean fileDownloaded = false;

        InputStream in = null;
        OutputStream out = null;
        FileOutputStream outputStream = null;//Added by Amit on 24-05-2010
        String inputFile = FiletoCopy;
        File outputFile = new File(ClientPath + "/" + FiletoCopy);

        int flag = 0;
        try {
            String inputfiles = inputFile;
            SOP("input list files is " + inputFile);

            ftp.setFileType(FTP.ASCII_FILE_TYPE);

            SOP(ftp.getReplyString());
            SOP("Remote system is " + ftp.getSystemName());

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            SOP("Array size is" + size);

            boolean FileFound = false;
            for (int i = 0; i < size; i++) {
                ftpfile = ftpFiles[i];
                String TempFileName = ftpFiles[i].getName();
                SOP("TempFileName :::" + TempFileName);
                if (TempFileName.equals(inputFile)) {
                    FileFound = true;
                    SOP("FileFound :::" + TempFileName);
                    break;
                }
            }

            if (!FileFound) {
                throw new Exception(inputFile + " not exists on server.");
            }

            try {
                outputStream = new FileOutputStream(outputFile);//Added by Amit on 24-05-2010
                boolean retValue = false;
                retValue = ftp.retrieveFile(inputFile, outputStream);
                fileDownloaded = retValue;
                if (!retValue) {
                    throw new Exception("Downloading of remote file " + inputFile + " failed. ftp.retrieveFile() returned false.");
                }
            } catch (Exception e) {
                fileDownloaded = false;
                SOP("The exection in function=" + e);
                throw new Exception("Downloading of remote file " + inputFile + " failed. ");
                //throw new Exception (e.getMessage());
            }
        } catch (Exception exe) {
            fileDownloaded = false;
            SOP("The exection in function=" + exe);
            SOP("The exection in function=" + exe.getMessage());
            if (exe != null && exe.getMessage().contains("not exists on server")) {
                //throw new Exception (exe.getMessage());
                throw new Exception("No file to download.");
            } else {
                throw new Exception("Downloading of remote file " + inputFile + " failed. ");
            }
            //throw new Exception (exe.getMessage());

        }
        if (outputStream != null) {
            outputStream.close();
        }//Added by Amit on 24-05-2010
        disConnect();
        return fileDownloaded;
    }

    //11
    public boolean downloadFile(String ServerPath, String ClientPath, String FiletoCopy) throws Exception {
        SOP("ServerPath=" + ServerPath);
        SOP("ClientPath=" + ClientPath);
        SOP("FiletoCopy=" + FiletoCopy);
        boolean fileDownloaded = false;

        InputStream in = null;
        OutputStream out = null;
        FileOutputStream outputStream = null;//Added by Amit on 24-05-2010
        String inputFile = FiletoCopy;
        File outputFile = new File(ClientPath + "/" + FiletoCopy);

        int flag = 0;
        try {
            String inputfiles = inputFile;
            SOP("input list files is " + inputFile);

            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            SOP(ftp.getReplyString());
            SOP("Remote system is " + ftp.getSystemName());

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            SOP("Array size is" + size);

            boolean FileFound = false;
            for (int i = 0; i < size; i++) {
                ftpfile = ftpFiles[i];
                String TempFileName = ftpFiles[i].getName();
                SOP("TempFileName :::" + TempFileName);
                if (TempFileName.equals(inputFile)) {
                    FileFound = true;
                    SOP("FileFound :::" + TempFileName);
                    break;
                }
            }

            if (!FileFound) {
                throw new Exception(inputFile + " not exists on server.");
            }

            try {
                outputStream = new FileOutputStream(outputFile);//Added by Amit on 24-05-2010
                boolean retValue = false;
                retValue = ftp.retrieveFile(inputFile, outputStream);
                fileDownloaded = retValue;
                if (!retValue) {
                    throw new Exception("Downloading of remote file " + inputFile + " failed. ftp.retrieveFile() returned false.");
                }
            } catch (Exception e) {
                SOP("The exection in function=" + e);
                throw new Exception("Downloading of remote file " + inputFile + " failed. ");
                //throw new Exception (e.getMessage());
            }
        } catch (Exception exe) {
            SOP("The exection in function=" + exe);
            SOP("The exection in function=" + exe.getMessage());
            if (exe != null && exe.getMessage().contains("not exists on server")) {
                //throw new Exception (exe.getMessage());
                throw new Exception("No file to download.");
            } else {
                throw new Exception("Downloading of remote file " + inputFile + " failed. ");
            }
            //throw new Exception (exe.getMessage());

        }
        if (outputStream != null) {
            outputStream.close();
        }//Added by Amit on 24-05-2010
        disConnect();
        return fileDownloaded;
    }

    /**
     * ********** Application Methods End here ****************
     */
    //11
    public boolean downloadDirectory(String parentDir,
            String currentDir, String saveDir) throws IOException {
        boolean success = true;
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        FTPFile[] subFiles = ftp.listFiles(dirToList);

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = parentDir + "/" + currentDir + "/"
                        + currentFileName;
                if (currentDir.equals("")) {
                    filePath = parentDir + "/" + currentFileName;
                }

                String newDirPath = saveDir + File.separator + currentDir + File.separator + currentFileName;

                if (currentDir.equals("")) {
                    newDirPath = saveDir + File.separator
                            + currentFileName;
                }

                if (aFile.isDirectory()) {
                    // create the directory in saveDir
                    File newDir = new File(newDirPath);
                    boolean created = newDir.mkdirs();
                    if (created) {
                        System.out.println("CREATED the directory: " + newDirPath);
                    } else {
                        System.out.println("COULD NOT create the directory: " + newDirPath);
                    }
                    // download the sub directory
                    downloadDirectory(dirToList, currentFileName,
                            saveDir + File.separator + currentDir);
                } else {
                    // download the file
                    success = downloadSingleFile(ftp, filePath,
                            newDirPath);
                    if (success) {
                        System.out.println("DOWNLOADED the file: " + filePath);
                    } else {
                        System.out.println("COULD NOT download the file: "
                                + filePath);
                    }
                }
            }
        }
        return success;
    }

    //11
    private boolean downloadSingleFile(FTPClient ftpClient,
            String remoteFilePath, String savePath) throws IOException {
        File downloadFile = new File(savePath);

        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdir();
        }

        OutputStream outputStream = new BufferedOutputStream(
                new FileOutputStream(downloadFile));
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient.retrieveFile(remoteFilePath, outputStream);
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    //11
    public boolean UploadFolder(String ServerPath, String ClientPath, String FolderName) throws Exception {
        SOP("ServerPath=" + ServerPath);
        SOP("ClientPath=" + ClientPath);
        SOP("FoldertoUpload=" + FolderName);

        boolean fileUploaded = false;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(ServerPath);
            SOP("ftp.getStatus() :: " + ftp.getStatus());
            SOP("ftp.printWorkingDirectory() :: " + ftp.printWorkingDirectory());
            //if ( !ftp.changeWorkingDirectory( FolderName ) )
            //    {
            //    ftp.makeDirectory( FolderName );
            //    ftp.changeWorkingDirectory(FolderName);
            //    }

            File inputFolder = new File(ClientPath + "/" + FolderName);
            fileUploaded = UploadFolder(inputFolder, ServerPath);

        } catch (Exception e) {
            System.out.println("The exection in function=" + e);
            throw new Exception("Folder upload fails. Error is -> " + e.getMessage());
        }
        System.out.println("fileUploaded :::::::::: " + fileUploaded);
        return fileUploaded;
    }

    //11
    public boolean UploadFolder(File sourceFolder, String DestinationFolder) throws Exception {
        boolean UploadCompleted = false;
        SOP("sourceFolder == " + sourceFolder.getName());
        SOP("DestinationFolder == " + DestinationFolder);
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(DestinationFolder);
            String FolderName = sourceFolder.getName();
            DestinationFolder = DestinationFolder + "/" + FolderName;
            if (!ftp.changeWorkingDirectory(FolderName)) {
                ftp.makeDirectory(FolderName);
                ftp.changeWorkingDirectory(FolderName);
            }
            if (sourceFolder.isDirectory()) {
                File[] files = sourceFolder.listFiles();
                if (files != null && files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isDirectory()) {
                            UploadFolder(files[i], DestinationFolder);
                        } else {
                            FileInputStream sourceFileStream = null;
                            sourceFileStream = new FileInputStream(files[i]);
                            UploadCompleted = ftp.storeFile(files[i].getName(), sourceFileStream);
                            sourceFileStream.close();
                        }
                    }

                }
            }
        } catch (Exception e) {
            System.out.println("The exection in function=" + e);
            throw new Exception("Folder upload fails. Error is -> " + e.getMessage());
        }
        // Step back up a directory once we're done with the contents of this one.
        try {
            ftp.changeToParentDirectory();
        } catch (IOException e) {
            throw new Exception("IOException caught while attempting to step up to parent directory"
                    + " after successfully processing " + sourceFolder.getAbsolutePath(),
                    e);
        }

        return UploadCompleted;
    }

    //11
    public String[] listOnlyFiles(String ServerPath) throws Exception {
        SOP("ServerPath::" + ServerPath);
        String ReadNewList[] = null;
        ArrayList readLineArray = null;
        int i = 0;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }

            readLineArray = new ArrayList();

            for (int j = 0; j < size; j++) {
                if (ftpFiles[j].isFile()) {
                    readLineArray.add(ftpFiles[j].getName() + "");
                }
            }

            ReadNewList = new String[readLineArray.size()];
            for (int j = 0; j < readLineArray.size(); j++) {
                ReadNewList[j] = readLineArray.get(j) + "";
                SOP(ReadNewList[j] + "");
            }
        } catch (Exception ex) {
            System.out.println("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        } finally {
            disConnect();
        }
        return ReadNewList;
    }

    //11
    public ArrayList<String> listOnlyDirectory(String ServerPath) throws Exception {
        ArrayList readLineArray = null;
        int i = 0;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }

            readLineArray = new ArrayList();

            for (int j = 0; j < size; j++) {
                if (ftpFiles[j].isDirectory() && !ftpFiles[j].getName().startsWith(".")) {
                    readLineArray.add(ftpFiles[j].getName());
                    SOP(ftpFiles[j].getName() + "");
                }
            }
        } catch (Exception ex) {
            System.out.println("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());

        } finally {
            //disConnect();
        }
        return readLineArray;
    }

    //11
    public boolean UploadFileJA(String FileNameWithPath, String ServerPath) throws Exception {
        boolean fileUploaded = false;
        SOP("ServerPath=" + ServerPath);
        SOP("FiletoUploadWithPath=" + FileNameWithPath);

        FileInputStream fis = null;
        File inputFile = new File(FileNameWithPath);

        if (!inputFile.exists() || !inputFile.isFile()) {
            throw new Exception(FileNameWithPath + " not found.");
        }

        try {
            fis = new FileInputStream(inputFile);

            SOP("input list files is " + inputFile.getName());

            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(ServerPath);

            boolean uplValue = false;
            uplValue = ftp.storeFile(inputFile.getName(), fis);
            fileUploaded = uplValue;

            if (!uplValue) {
                throw new Exception("Unable to upload file " + inputFile.getParent() + " failed. ftp.storeFile() returned false.");
            }
            ///
            // Check ZIP  and compare file size...
            int CheckResult = 0;
            if (FileNameWithPath.indexOf(".tar") > 0 || FileNameWithPath.indexOf(".zip") > 0 || FileNameWithPath.indexOf(".sit") > 0 || FileNameWithPath.indexOf(".sitx") > 0 || FileNameWithPath.indexOf(".eps") > 0) {
                //CheckResult=CompareFiles.check(ClientPath+"/"+FiletoUpload, ftp.retrieveFileStream(FiletoUpload));	//For PC
            }
            if (CheckResult == 1) {
                throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
            }
            ///
            ///
        } catch (Exception e) {
            if (e.getMessage().indexOf("problem file is") > 0) {
                throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
            }
            SOP("The exection in function=" + e);
        }
        disConnect();
        return fileUploaded;
    }

    /**
     * ********************** LaunchArtApplet.java Methods *******************
     */
    //11
    public String[] listAllFilesIncludingDirAlso(String ServerPath) throws Exception {
        String readLineArray[] = null;
        int i = 0;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }

            readLineArray = new String[size];

            for (int j = 0; j < size; j++) {
                readLineArray[j] = ftpFiles[j].getName() + "";
            }
        } catch (Exception ex) {
            SOP("Error :: " + ex.getMessage());
            throw new Exception(ex.getMessage());

        } finally {
            disConnect();
        }
        return readLineArray;
    }

    //11
    public boolean checkIfDirectory(String ServerPath, String fName) {
        boolean isDirectory = false;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            } else {
                throw new Exception("Error in change directory for - " + ServerPath);
            }

            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new Exception("Unable to change working directory " + "to:" + ServerPath);
            }
            for (int j = 0; j < size; j++) {
                if (ftpFiles[j].getName().equals(fName) && ftpFiles[j].isDirectory()) {
                    isDirectory = true;
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isDirectory;
    }

    public static void main(String[] args) throws Exception {
        /*
         FTPClient ftp = new FTPClient();
         ftp.setDefaultPort(21);
         ftp.connect("apollo.aptaracorp.com");
         ftp.login("brokerapo", "p6sJhk4t");
         */
        //  ApacheSFtp sftp1 = new ApacheSFtp("192.168.192.117", "brokeratlas", "Atlas#2019", "sftp");

        ApacheSFtp sftp2 = new ApacheSFtp("apollo.aptaracorp.com", "brokerapo", "p6sJhk4t", "sftp");
        
        sftp2.listAllforFolder("/apol7/PowerManage/dev/TFJ-UK/TMPH/TMPH_00_00/TMPH_GN_47003","sftp");
  System.out.println("1: "+sftp2);      
        sftp2.disConnect();
  System.out.println("2: "+sftp2);
       // String[] files = sftp2.listOnlyFiles("/apol21/PowerManage/ELVR/SIGPRO/SIGPRO_169_C/P100//ELD", null);
       
       // System.out.println(sftp2.lastModifiedFileName("/apol25/PowerManage/ELVR/CJPH/CJPH_00_00/CJPH1003"));
//System.out.println("end");
       // sftp2.downloadFile("/apol25/PowerManage/ELVR/CJPH/CJPH_00_00/CJPH1003", "C:/broker/ELVR/CJPH/CJPH_00_00/CJPH1003", "CJPH1003.log", null);
        //sftp1.downloadLastModifiedDir("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/ENDEND8770/order/PROOF", "/apol19/PowerManage/dev/ELVR/", "sftp");
        //sftp1.listAllforFolder("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/ENDEND282", "sftp");
        // sftp2.listAllforFolder("/apol7/PowerManage/dev/TFJ-UK/TMPH/TMPH_00_00/TMPH_GN_47003","sftp");

        //  sftp2.UploadFileJA("D:\\Sushil\\Temp\\ENDEND282.zip", "/apol7/PowerManage/dev/TFJ-UK/TMPH/TMPH_00_00/TMPH_GN_47003", null);
        //sftp1.UploadFileJA("D:\\Sushil\\Temp\\ENDEND282.zip", "/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/ENDEND282", null);
        //sftp1.listOnlyDirectory("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/ENDEND282", null);
        //sftp2.listOnlyDirectory("/apol7/PowerManage/dev/TFJ-UK/TMPH/TMPH_00_00/TMPH_GN_47003");
        // sftp2.listOnlyFiles("/apol7/PowerManage/dev/TFJ-UK/TMPH/TMPH_00_00/TMPH_GN_47003");
        //sftp1.listOnlyFiles("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/ENDEND282", null);
        //System.out.println("IsUloaded: " + sftp1.UploadFolder("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/ENDEND282", "D:\\Sushil\\Temp", "AA", null));
        //System.out.println("IsUloaded: "+sftp2.UploadFolder("/apol7/PowerManage/dev/TFJ-UK/TMPH/TMPH_00_00/TMPH_GN_47003", "D:\\Sushil\\Temp", "AA", null));
        // System.out.println("IsDownloaded: "+sftp2.downloadSingleFile(ftp, "/apol7/PowerManage/dev/TFJ-UK/TMPH/TMPH_00_00/TMPH_GN_47003/IEEEAutoJem25-11-19.log", "D:\\Sushil\\Temp\\IEEEAutoJem25-11-19.log", null));
        // System.out.println("IsDownloaded: "+sftp1.downloadSingleFile(ftp, "/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/ENDEND282/_JBO100271.xml", "D:\\Sushil\\Temp"+File.separator+"_JBO100271.xml", null));
        // System.out.println(":::--> "+sftp2.downloadDirectory("/apol7/PowerManage/dev/TFJ-UK/TMPH/TMPH_00_00/TMPH_GN_47003", "art", "D:\\Sushil\\Temp", null));
        // sftp1.downloadDirectory("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/APTEND110", "AA", "D:\\Sushil\\Temp", null);
        /*
         System.out.println(":::--> "+sftp1.CheckPathExistence("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/APTEND110","sftp"));
    
         System.out.println("::::::::::::::::::::::::::::::::::::::\n");
    
         sftp1.listDirectoryWithMetadata("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/APTEND110", "sftp");
    
         System.out.println("::::::::::::::::::::::::::::::::::::::\n");
    
         sftp1.listFilesWithMetaData("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/APTEND110", "sftp");
         */
        // System.out.println("::::::::::::::::::::::::::::::::::::::\n");
        // System.out.println(sftp1.CheckIsDir("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/APTEND110", "ELD", "sftp"));
        //  sftp1.downloadFolder("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/APTEND110", "d://Sushil//temp", "ELD", "sftp");
        //    sftp1.downloadFileForFolder("/atlas1/PowerManage/dev/ELVR/ENDEND/ENDEND_00_00/APTEND110", "d://Sushil//temp", "APTEND110.docx", "sftp");
        /**
         * *********************************************************************************
         */
        /*
         ApacheSFtp sftp2 = new ApacheSFtp("apollo.aptaracorp.com", "brokerapo", "p6sJhk4t", "0000");
    
         System.out.println(":::--> "+sftp2.CheckPathExistence("/apol7/PowerManage/dev/TFJ-UK/TMPH/TMPH_00_00/TMPH_GN_47003"));
    
         sftp2.listDirectoryWithMetadata("/apol7/PowerManage/dev/TFJ-UK/TMPH/TMPH_00_00", "0000");
    
         sftp2.listFilesWithMetaData("/apol7/PowerManage/dev/TFJ-UK/TMPH/TMPH_00_00/TMPH_GN_47003", "0000");
         */
        
        

            
    }
    /**
     * ********** Application Methods End here ****************
     */
    int ShowSOP = 1;

    public void SOP(String message2print) {
        if (ShowSOP == 1) {
            System.out.println(message2print);
        }
    }
    //*** End ***
}

