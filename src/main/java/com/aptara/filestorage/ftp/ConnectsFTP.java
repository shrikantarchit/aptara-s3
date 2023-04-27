/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.aptara.filestorage.ftp;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 *
 * @author LOKESH.PALIWAL
 */
public class ConnectsFTP {
       
    Session     session     = null;    
    ChannelSftp channelSftp = null;    
    public ConnectsFTP(String SFTPHOST,int SFTPPORT,String SFTPUSER,String SFTPPASS,String SFTPWORKINGDIR) throws Exception{                
            JSch jsch = new JSch();            
            session = jsch.getSession(SFTPUSER,SFTPHOST,SFTPPORT);
            session.setPassword(SFTPPASS);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            if(!session.isConnected()){
                throw new Exception("Unable to connect to server,Please try again.");
            }
            Channel  channel = session.openChannel("sftp");
            channel.connect();                           
            channelSftp = (ChannelSftp)channel;
            channelSftp.cd("/");       
    }
           
    /**
     * This method list the file names based on the server path passed.
     * SFtp sesssion is disconnected in this method.
     * @param SFTPWORKINGDIR 
     * @return ArrayList     
     * @throws Exception
     **/
    public ArrayList listAll(String SFTPWORKINGDIR) throws Exception{
        ArrayList<String> arraylist = null;
        try{
            if(channelSftp.isConnected()){
                channelSftp.cd(SFTPWORKINGDIR);
                Vector<ChannelSftp.LsEntry> list = channelSftp.ls(SFTPWORKINGDIR);
                if(list!=null && !list.isEmpty()){
                    arraylist = new ArrayList<String>();
                    for (ChannelSftp.LsEntry oListItem : list) { 
                    	if (!(".").equals(oListItem.getFilename()) &&  !("..").equals(oListItem.getFilename()))
            			{
                    		System.out.println("oListItem.getFilename()"+oListItem.getFilename());
            				arraylist.add(oListItem.getFilename());
            			}
                    }
                }
            }else{
                throw new Exception("SFtp server is not connected.");
            }   
        }finally{
            disConnect();
        }
        return arraylist;
    }
    
    /**
     * This method list the file names based on the server path passed.
     * SFtp sesssion is disconnected in this method.
     * @param SFTPWORKINGDIR 
     * @return ArrayList     
     * @throws Exception
     **/
    public ArrayList listOnlyFiles(String SFTPWORKINGDIR)throws Exception{
        ArrayList<String> arraylist = null;
        try{
            if(channelSftp.isConnected()){                
                channelSftp.cd(SFTPWORKINGDIR);                
                Vector<ChannelSftp.LsEntry> list = channelSftp.ls(SFTPWORKINGDIR);
                if(list!=null && !list.isEmpty()){
                    arraylist = new ArrayList<String>();
                    for (ChannelSftp.LsEntry oListItem : list) { 
                        if (!oListItem.getAttrs().isDir()) {                             
                        	if (!(".").equals(oListItem.getFilename()) &&  !("..").equals(oListItem.getFilename())){
                             //System.out.println("FileName :"+oListItem.getFilename()); 
	                             arraylist.add(oListItem.getFilename());
	                        }
                        }
                    }
                }
            }else{
                throw new Exception("SFtp server is not connected.");
            }
        }finally{
            disConnect();
        }
        return arraylist;
    }
    
    /**
     * This method list directory based on the server path passed.
     * @param SFTPWORKINGDIR          
     * @return List
     * @throws com.jcraft.jsch.JSchException
     **/            
    public List listDirectory(String SFTPWORKINGDIR)throws Exception{  
            List<String> arraylist = null;                        
            try{
                if(channelSftp.isConnected()){
                    channelSftp.cd(SFTPWORKINGDIR);
                    Vector<ChannelSftp.LsEntry> list = channelSftp.ls(SFTPWORKINGDIR);
                    if(list!=null && !list.isEmpty()){
                        arraylist = new ArrayList<String>();
                        for (ChannelSftp.LsEntry oListItem : list) {            
                        	if (!(".").equals(oListItem.getFilename()) &&  !("..").equals(oListItem.getFilename())){                    
                                //System.out.println("Directory :"+oListItem.getFilename()); 
                                arraylist.add(oListItem.getFilename());
                            } 
                        }
                    }   
                }else{
                    throw new Exception("SFtp server is not connected.");
                }
            }finally{
                disConnect();
            }
            return arraylist;
    }        
    
    /**
     * This method upload file to server path passed.
     * SFtp sesssion is disconnected in this method.
     * @param SFTPWORKINGDIR 
     * @param  FiletoUpload 
     * @param FileUpload 
     * @return boolean
     * @throws Exception
     **/
    public boolean UploadFile(String SFTPWORKINGDIR,String FiletoUpload,InputStream FileUpload)throws Exception{
        boolean isUploaded = false;
        try{
            if(channelSftp.isConnected()){ 
                channelSftp.cd(SFTPWORKINGDIR);
                channelSftp.put(FileUpload, FiletoUpload);                 
                if(CheckFileExist(SFTPWORKINGDIR,FiletoUpload)){
                    isUploaded = true;
                }
            }else{
                throw new Exception("SFtp server is not connected.");
            }
        }finally{
            disConnect();
        }        
        return isUploaded;
    }
    
    /**
     * This method validate file existence on server location
     * @param SFTPWORKINGDIR
     * @param creteria 
     * @return boolean
     * @throws java.lang.Exception
     **/
    public boolean CheckFileExist(String SFTPWORKINGDIR,String creteria)throws Exception{
        boolean isValidated = false;
        if(channelSftp.isConnected()){
            try{
                channelSftp.cd(SFTPWORKINGDIR);
                Vector<ChannelSftp.LsEntry> list = channelSftp.ls(creteria);
                if(list!=null && !list.isEmpty()){
                    isValidated = true;
                }
            }catch(Exception caught){}
        }else{
                throw new Exception("SFtp server is not connected.");
        }       
        //System.out.println("isValidated-->"+isValidated);
        return isValidated;        
    }
           
    /**
     * This method check for the existence of folder on the server path passed
     * if not exist than create it otherwise return true.SFtp session is not 
     * disconnected in the method.
     * @param ServerPath 
     * @param FolderName 
     * @return boolean
     * @throws java.lang.Exception
     ***/
   /* public boolean CreateFolder(String ServerPath, String FolderName)throws Exception{
        boolean isCreated = false;       
        //System.out.println("CreateFolder--> "+ServerPath+"\t"+FolderName);
        if(channelSftp.isConnected()){
            if(!CheckFileExist(ServerPath, FolderName)){
                channelSftp.cd(ServerPath); 
                channelSftp.mkdir(FolderName);
                if(CheckFileExist(ServerPath, FolderName)){
                    isCreated = true;
                }
            }else{
                isCreated = true;
            }
        }else{
                throw new Exception("SFtp server is not connected.");
        }            
        return isCreated;
    }*/
    
  public boolean CreateFolder(String ServerPath, String FolderName)throws Exception{
        boolean isCreated = false;
        //System.out.println("CreateFolder--> "+ServerPath+"\t"+FolderName);
        if(channelSftp.isConnected()){
        	String[] folders = ServerPath.split("/");
        	  for (String folder : folders) {
        	    if (folder.length() > 0 && !folder.contains(".")) {
        	      // This is a valid folder:
        	      try {
        	    	  channelSftp.cd(folder);
        	      } catch (SftpException e) {
        	        // No such folder yet:
        	    	  channelSftp.mkdir(folder);
        	    	  channelSftp.cd(folder);
        	      }
        	    }
        	  }
        	  if(!CheckFileExist(ServerPath, FolderName)){
                channelSftp.cd(ServerPath);
                channelSftp.mkdir(FolderName);
                if(CheckFileExist(ServerPath, FolderName)){
                    isCreated = true;
                }
            }else{
                isCreated = true;
            }
        }else{
                throw new Exception("SFtp server is not connected.");
        }
        return isCreated;
    }  
    
    /**
     * This method check the file on the server path passed and 
     * if found than retrieve the InputStream other throw Exception. 
     * SFtp session is not disconnected in the method.
     * @param productionServerFilePath
     * @param FileName
     * @return InputStream
     * @throws java.lang.Exception
     **/
    public InputStream getInputStream(String productionServerFilePath, String FileName) throws Exception{
        InputStream iStream = null;        
        if(channelSftp.isConnected()){
            if(CheckFileExist(productionServerFilePath, FileName)){                                    
                channelSftp.cd(productionServerFilePath);
                iStream = channelSftp.get(FileName);                
            }else{
                throw new Exception("Requested does not exits on the server path "+productionServerFilePath);
            }
        }else{
                throw new Exception("SFtp server is not connected.");
        }        
        return iStream;
    }
            
    /**
     * This method will delete the file on the given server path passed.
     * @param productionServerFilePath
     * @param FileNameToDel
     * @return Boolean
     * @throws java.lang.Exception
     **/
    public boolean deleteFile(String productionServerFilePath, String FileNameToDel) throws Exception{
        boolean fileDeleted = false;        
        if(channelSftp.isConnected()){
            if(CheckFileExist(productionServerFilePath, FileNameToDel)){                                    
                channelSftp.cd(productionServerFilePath);
                channelSftp.rm(FileNameToDel);
                if(!CheckFileExist(productionServerFilePath, FileNameToDel)){
                	fileDeleted=true;
                }
            }else{
                throw new Exception("Requested does not exits on the server path "+productionServerFilePath);
            }
        }else{
                throw new Exception("SFtp server is not connected.");
        }        
        return fileDeleted;
    }
    
    
   /* public boolean deleteDirectory(String productionServerFilePath, String FileNameToDel) throws Exception{
        boolean fileDeleted = false;        
        if(channelSftp.isConnected()){
            if(CheckFileExist(productionServerFilePath, FileNameToDel)){                                    
                channelSftp.cd(productionServerFilePath);
                channelSftp.rm(FileNameToDel);
                if(!CheckFileExist(productionServerFilePath, FileNameToDel)){
                	fileDeleted=true;
                }
            }else{
                throw new Exception("Requested does not exits on the server path "+productionServerFilePath);
            }
        }else{
                throw new Exception("SFtp server is not connected.");
        }        
        return fileDeleted;
    }*/
	
    public boolean DeleteDirectory(String path)throws Exception {
        boolean dirDeleted=false;
       try{
           if(channelSftp.isConnected()){
                    SftpATTRS attrs = channelSftp.stat(path);  
                    if (attrs.isDir()) {
                        Vector<LsEntry> files = channelSftp.ls(path);
                        if (files != null && files.size() > 0) {
                            Iterator<LsEntry> it = files.iterator();
                            while (it.hasNext()) {
                                LsEntry entry = it.next();
                                if ((!entry.getFilename().equals(".")) && (!entry.getFilename().equals(".."))) {
                                    DeleteDirectory(path + "/" + entry.getFilename());
                                }
                            }
                        }
                        channelSftp.rmdir(path);
                    } else {
                        channelSftp.rm(path);
                    }
                    dirDeleted=true;
             }else{
                 throw new Exception("SFtp server is not connected.");
             } 
         }catch(Exception e){
             throw new Exception(e.getLocalizedMessage());
       }
       System.out.println("Deletion for "+path+ " , status: "+dirDeleted);
       return dirDeleted;
    }

    
    public boolean disConnect(){
        boolean isDisconnected = false;
        try{
            System.out.println("disConnect");
            if(channelSftp!=null){channelSftp.disconnect();channelSftp=null;}
            if(session!=null){session.disconnect();session=null;isDisconnected=true;}            
        }catch(Exception caught){caught.printStackTrace();}
        return isDisconnected;
    }
    
     @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        String SFTPHOST = "oxpzaps20l-sftp.elsevier.co.uk";
        int    SFTPPORT = 22;
        String SFTPUSER = "aptar";
        String SFTPPASS = "aptarge6491";
        String SFTPWORKINGDIR = "/ftpdata/outputbuckets/APTAR";

        Session     session     = null;
        Channel     channel     = null;
        InputStream is = null;
        ConnectsFTP cf =null;
        try{            
            //cf = new ConnectsFTP(SFTPHOST, SFTPPORT, SFTPUSER, SFTPPASS, SFTPWORKINGDIR);
            //File f = new File("C:/broker/APSPMPQProcess_Log_23_Jul_2015.doc");
            //System.out.println("####-->"+cf.UploadFile(SFTPWORKINGDIR,f.getName(), new FileInputStream(f)));                       
        }catch(Exception e){e.printStackTrace();}finally{
            try{
                if(is!=null){is.close();is=null;}   
                cf.disConnect();
            }catch(Exception e){}
        }            
    }
    
}