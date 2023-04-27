package com.aptara.filestorage.ftp;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

//import uplDownLoadApplet.ClientTaskConfig;
public class ApacheFtp {

    FTPClient ftp = new FTPClient();

    public ApacheFtp(String ftpHost, String ftpUserName, String ftpPassword) throws IOException,Exception
	{
	       ftp.connect(ftpHost);
	       int reply;
	       reply = ftp.getReplyCode();
	       if(!FTPReply.isPositiveCompletion(reply))
	       	{
	            try
	            {
	                ftp.disconnect();
	            }catch (Exception e)
	            {
	            	System.err.println("Unable to disconnect from server after server refused connection. "+e.toString());
	            }
	            throw new Exception ("FTP server refused connection.");
	       	}
	       //SOP("Connected to " + ftpHost + ". "+ftp.getReplyString());
	        if (!ftp.login(ftpUserName, ftpPassword))
	        {
	            throw new Exception ("Unable to connect Production Server");
        	}
	        //Updated by Atul to keep local in passive mode
	        ftp.enterLocalPassiveMode();
	}

    public Hashtable listFileOnRootDirWithTimeStamp(String path) throws Exception {
        Hashtable readLineArray = null;
        int i = 0;
        try {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile ftpfile = new FTPFile();

            ftpfile.setRawListing(".");
            boolean changeDir = ftp.changeWorkingDirectory(path);
            FTPFile[] ftpFiles = null;
            if (changeDir) {
                ftpFiles = ftp.listFiles();
            }else{
            	SOP("Unable to change the directory : " + path);
            }
            int size = (ftpFiles == null) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP(size + "-->reply code" + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                System.out.println("Failed in changing");
            }

            readLineArray = new Hashtable<String, Calendar>();
            SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
            
            for (int j = 0; j < size; j++) {                
                if (ftpFiles[j].isFile()) {
                	Date fileDate = ftpFiles[j].getTimestamp().getTime();
                	String date = fmt.format(fileDate.getTime());
                	readLineArray.put(ftpFiles[j].getName(), date);
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
    
    public  void  copyFile(File outputFile,String inputFile)
	{
        int reply;
        //InputStream in = null;
        //OutputStream out = null;
        int flag =0;
        try
        {
            File outputFile1 = new File(outputFile+"");
            boolean success = (new File(outputFile+"")).mkdirs();

            String inputfiles = inputFile;
            SOP("input list files is " + inputFile);

            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            SOP(ftp.getReplyString());
            //SOP("Remote system is " + ftp.getSystemName());

            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(inputFile);

            FTPFile[] ftpFiles = ftp.listFiles( inputFile );

            if (inputFile != null && inputFile.trim().length() > 0)
            {
                    ftp.changeWorkingDirectory(inputFile);
                    reply = ftp.getReplyCode();
                    SOP("reply code" + reply);
                    if(!FTPReply.isPositiveCompletion(reply))
                    {
                            throw new Exception ("Unable to change working directory to : "+inputFile);
                    }
            }
            SOP("directory is created..." + success);
            SOP("after input file .." + inputFile);
            SOP("input for length" + inputFile.length());
            int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            SOP("Array size is" + size);

            ArrayList ar  = new ArrayList();

            for( int i = 0; i < size; i++ )
            {
                 ftpfile = ftpFiles[i];

                 ar.add(ftpFiles[i].getName());

                 SOP("file name & size " + ftpfile.getName()      + ":"  + ftpfile.getSize());
                 SOP("file name & directory "+ ftpfile.getName()  + ":"  + ftpfile.isDirectory());
            }

            if(ftpfile.isDirectory())
            {
                String temIn =  inputFile+"";
                SOP("The now dir="+temIn);
                SOP("The now dir="+outputFile+"");
                filecopy(temIn,outputFile+"");
            }
            else
            {
            	SOP("The input file="+inputFile);
            	SOP("\nThe outpit file="+outputFile);
                try
                {
                    boolean retValue=false;
                    Iterator it  = ar.iterator();
                    SOP("file name is " + ftpfile.getName());
                    while(it.hasNext())
                    {
                        String filename = (String) it.next();
                        SOP("file name is" + filename);

                        String File1 = outputFile + "\\"+ filename;
                        SOP("out file is " + File1);

                        File  fileout = new File(File1);
                        retValue = ftp.retrieveFile(filename, new FileOutputStream(fileout));
                    }
                    if (!retValue)
                    {
                          throw new Exception ("Downloading of remote file "+ inputFile+" failed. ftp.retrieveFile() returned false.");
                    }
                }
                catch(Exception e)
                {
                	SOP("The exection in function="+e);
                }
            }
        }
        catch(Exception exe)
        {
        	SOP("The exection in function="+exe);
        }
    }

   /**************************START******************************/
    public  boolean  downloadFile(String ServerPath, String ClientPath, String FiletoCopy)  throws Exception
    {
	   SOP("ServerPath="+ServerPath);
	   SOP("ClientPath="+ClientPath);
	   SOP("FiletoCopy="+FiletoCopy);
	   boolean fileDownloaded = false;

	   InputStream in = null;
	   OutputStream out = null;
	   FileOutputStream outputStream=null;//Added by Amit on 24-05-2010
	   String inputFile = FiletoCopy;
	   File outputFile = new File(ClientPath+"/"+FiletoCopy);

	   int flag =0;
	   try
   		{
		   String inputfiles = inputFile;
		   SOP("input list files is " + inputFile);

		   ftp.setFileType(FTP.BINARY_FILE_TYPE);

		   SOP(ftp.getReplyString());
		   SOP("Remote system is " + ftp.getSystemName());

		   FTPFile ftpfile = new FTPFile();
		   ftpfile.setRawListing(ServerPath);

                    boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                    FTPFile[] ftpFiles = null;
                    if(changeDir)
                        {
                        ftpFiles = ftp.listFiles();
                        }
                    else
                        {
                        throw new Exception("Error in change directory for - "+ServerPath);
                        }

		   int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

		   SOP("Array size is" + size);

		   boolean FileFound = false;
                    for (int i = 0; i < size; i++) {
                        if (ftpFiles[i] != null && !ftpFiles[i].getName().equalsIgnoreCase("null")) {
                            ftpfile = ftpFiles[i];
                            String TempFileName = ftpFiles[i].getName();
                            SOP("TempFileName :::" + TempFileName);
                            if (TempFileName.equals(inputFile)) {
                                FileFound = true;
                                SOP("FileFound :::" + TempFileName);
                                break;
                            }
                        }
                    }

		   if(! FileFound)
		   {
			   throw new Exception(inputFile + " not exists on server.");
		   }

		   try
		   {
			   outputStream=new FileOutputStream(outputFile);//Added by Amit on 24-05-2010
			   boolean retValue=false;
				retValue = ftp.retrieveFile(inputFile, outputStream);
				fileDownloaded = retValue;
                if (!retValue)
            	{
                    throw new Exception ("Downloading of remote file "+ inputFile+" failed. ftp.retrieveFile() returned false.");
            	}
		   }catch(Exception e)
		   {
			   SOP("The exection in function="+e);
			   throw new Exception ("Downloading of remote file "+ inputFile+" failed. ");
			   //throw new Exception (e.getMessage());
		   }
   		}catch(Exception exe)
   		{
   			SOP("The exection in function="+exe);
   			throw new Exception ("Downloading of remote file "+ inputFile+" failed. ");
   			//throw new Exception (exe.getMessage());

   		}
   		if(outputStream!=null){	outputStream.close();}//Added by Amit on 24-05-2010
   		disConnect();
   		return fileDownloaded;
	}

   /**********************************************************/
   //public  void  copyFile(File outputFile,String inputFile)
public  boolean downloadFileForFolder(String ServerPath, String ClientPath, String FiletoCopy)  throws Exception
{
	SOP("ServerPath="+ServerPath);
	SOP("ClientPath="+ClientPath);
	SOP("FiletoCopy="+FiletoCopy);
	boolean fileDownloaded = false;

	String inputFile = FiletoCopy;
	File outputFile = new File(ClientPath+"/"+FiletoCopy);

	int flag =0;
	try
   	{
        String inputfiles = inputFile;
        SOP("input list files is " + inputFile);

       ftp.setFileType(FTP.BINARY_FILE_TYPE);

        SOP(ftp.getReplyString());
        SOP("Remote system is " + ftp.getSystemName());

        FTPFile ftpfile = new FTPFile();
        ftpfile.setRawListing(ServerPath);

        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
        FTPFile[] ftpFiles = null;
        if(changeDir)
            {
            ftpFiles = ftp.listFiles();
            }
        else
            {
            throw new Exception("Error in change directory for - "+ServerPath);
            }
        //SOP("ftpFiles.LENGTH=========== "+ftpFiles.length);
       int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

		SOP("Array size is  " + size);

        boolean FileFound = false;
        for( int i = 0; i < size; i++ )
        {
            ftpfile = ftpFiles[i];
            String TempFileName = ftpFiles[i].getName();
            if(TempFileName.equals(inputFile))
            {
            	FileFound = true;
            	SOP("FileFound :::" + TempFileName);
            	break;
            }
        }
        if(! FileFound)
  		{
        	throw new Exception(inputFile + " not exists on server.");
  		}

        if(ftpFiles == null || size < 1)
        {
        	throw new Exception("FTP connection closed");
        }

		try
		{
                boolean retValue=false;
                SOP("file name is " + ftpfile.getName());
                try
                {
				retValue = ftp.retrieveFile(inputFile, new FileOutputStream(outputFile));
                }catch(Exception e)
                {
                	SOP("Exception in downloading file ===>"+e);
                }
				fileDownloaded = retValue;

                if (!retValue)
                {
                    throw new Exception ("Downloading of remote file "+ inputFile+" failed. "+ftp.isConnected());
                }
		} catch(Exception e)
		{
			SOP("The exection in function="+e);
		}
	} catch(Exception exe)
	{
		SOP("The exection in function="+exe);
	}
	SOP("Download completed for -- "+FiletoCopy);
	return fileDownloaded;
}

 //####222222
/*************** Added on 30th July 2009 - JA File Upload *****************************************/
public boolean UploadFileJA(String FileNameWithPath, String ServerPath) throws Exception
{
	boolean fileUploaded = false;
	SOP("ServerPath="+ServerPath);
	SOP("FiletoUploadWithPath="+FileNameWithPath);

      FileInputStream fis = null;
      File inputFile = new File(FileNameWithPath);

      if(! inputFile.exists() || !inputFile.isFile())
      	{
    	  throw new Exception(FileNameWithPath + " not found.");
      	}

      try
      {
    	fis = new FileInputStream(inputFile);

    	SOP("input list files is " + inputFile.getName());

       ftp.setFileType(FTP.BINARY_FILE_TYPE);
       ftp.changeWorkingDirectory(ServerPath);

		boolean uplValue=false;

		uplValue = ftp.storeFile(inputFile.getName(), fis);
		fileUploaded = uplValue;

        if (!uplValue)
    	{
            throw new Exception ("Unable to upload file "+ inputFile.getParent()+" failed. ftp.storeFile() returned false.");
        }
        ///
        // Check ZIP  and compare file size...
       	int CheckResult = 0;
       	if(FileNameWithPath.indexOf(".tar") > 0 || FileNameWithPath.indexOf(".zip") > 0 || FileNameWithPath.indexOf(".sit") > 0 || FileNameWithPath.indexOf(".sitx") > 0 || FileNameWithPath.indexOf(".eps") > 0)
   		{
       		//CheckResult=CompareFiles.check(ClientPath+"/"+FiletoUpload, ftp.retrieveFileStream(FiletoUpload));	//For PC
   		}
       	if(CheckResult==1)
   		{
       		throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
   		}
       			///
        ///
	} catch(Exception e)
	{
		if(e.getMessage().indexOf("problem file is")>0)
			throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
	   SOP("The exection in function="+e);
	}
      		disConnect();
      		return fileUploaded;
}
   /**********************************************************/
	public boolean UploadFile(String ServerPath, String ClientPath, String FiletoUpload) throws Exception
	{
		boolean fileUploaded = false;
		SOP("ServerPath="+ServerPath);
		SOP("ClientPath="+ClientPath);
		SOP("FiletoUpload="+FiletoUpload);

	      FileInputStream fis = null;
	      File inputFile = new File(ClientPath+"/"+FiletoUpload);

	      if(! inputFile.exists() || !inputFile.isFile())
	      	{
	    	  throw new Exception(FiletoUpload + " not found.");
	      	}

	      try
	      {
	    	fis = new FileInputStream(inputFile);

	    	SOP("input list files is " + inputFile.getName());

           ftp.setFileType(FTP.BINARY_FILE_TYPE);
           ftp.changeWorkingDirectory(ServerPath);

			boolean uplValue=false;

			uplValue = ftp.storeFile(inputFile.getName(), fis);
			fileUploaded = uplValue;

	        if (!uplValue)
        	{
	            throw new Exception ("Unable to upload file "+ inputFile.getParent()+" failed. ftp.storeFile() returned false.");
            }
	        ///
	        // Check ZIP  and compare file size...
	       	int CheckResult = 0;
	       	if(FiletoUpload.indexOf(".tar") > 0 || FiletoUpload.indexOf(".zip") > 0 || FiletoUpload.indexOf(".sit") > 0 || FiletoUpload.indexOf(".sitx") > 0 || FiletoUpload.indexOf(".eps") > 0)
       		{
	       		//CheckResult=CompareFiles.check(ClientPath+"/"+FiletoUpload, ftp.retrieveFileStream(FiletoUpload));	//For PC
       		}
	       	if(CheckResult==1)
       		{
	       		throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
       		}
	       			///
	        ///
		} catch(Exception e)
		{
			if(e.getMessage().indexOf("problem file is")>0)
				throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
		   SOP("The exection in function="+e);
		}
	      		disConnect();
	      		return fileUploaded;
   	}

/**************** New method by Atul - to upload a file with InputStream  ********************************/
public boolean UploadFileWithCheckSum(String ServerPath,  String FiletoUpload,InputStream FileUpload, InputStream FileUpload2) throws Exception
   		{
		boolean fileUploaded = false;
                InputStream CopyFileUpload = FileUpload2;
SOP("ServerPath="+ServerPath);
SOP("FiletoUpload="+FiletoUpload);
          try
	      	{
           ftp.setFileType(FTP.BINARY_FILE_TYPE);
           ftp.changeWorkingDirectory(ServerPath);

			boolean uplValue=false;

			uplValue = ftp.storeFile(FiletoUpload, FileUpload);
			fileUploaded = uplValue;

	        if (!uplValue)
	        	{
	            throw new Exception ("Unable to upload file "+ FiletoUpload+" failed. ftp.storeFile() returned false.");
	            }

            int CheckResult = 0;
	       		CheckResult=CompareFiles.check(CopyFileUpload, ftp.retrieveFileStream(FiletoUpload));

                        //FileUpload.close();
	       	if(CheckResult==1)
	       		{
	       		throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
	       		}

	   			} catch(Exception e)
	   					{
	   				if(e.getMessage().indexOf("problem file is")>0)
	   					throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
	                   SOP("The exection in function="+e);
	   					}
	      		disConnect();
	      		return fileUploaded;
	   	}
/**********************************************************/

/**********************************************************/
	public boolean UploadFileCTC(String ServerPath, String ClientPath, String FiletoUpload) throws Exception
	{
		SOP("ServerPath="+ServerPath);
		SOP("ClientPath="+ClientPath);
		SOP("FiletoUpload="+FiletoUpload);
		  boolean fileUploaded = false;
		  SOP("yyyy---------"+ftp.isConnected());
	      FileInputStream fis = null;
	      File inputFile = new File(ClientPath+"/"+FiletoUpload);
	      if(! inputFile.exists() || !inputFile.isFile())
  			{
	    	  	throw new Exception(FiletoUpload + " not found.");
      		}

	      try
	      {
	    	fis = new FileInputStream(inputFile);
	    	SOP("input list files is " + inputFile.getName());

	    	ftp.setFileType(FTP.BINARY_FILE_TYPE);
	    	SOP("ftp.getStatus() :: "+ftp.getStatus());

	    	ftp.changeWorkingDirectory(ServerPath);

            //SOP(ftp.getReplyString());
            //SOP("Remote system is " + ftp.getSystemName());
            SOP("ftp.getStatus() :: "+ftp.getStatus());
            SOP("ftp.printWorkingDirectory() :: "+ftp.printWorkingDirectory());

			boolean uplValue=false;

			uplValue = ftp.storeFile(inputFile.getName(), fis);
			fileUploaded = uplValue;
	        if (!uplValue)
        	{
	        	SOP("Unable to upload file "+ inputFile.getName());
	            throw new Exception ("Unable to upload file "+ inputFile.getParent()+ " failed. ftp.storeFile() returned false.");
            }
	        if(fis!=null)
	        	fis.close();
	        inputFile=null;
	        ///
	        // Check ZIP  and compare file size...
	       	int CheckResult = 0;
	       	if(FiletoUpload.indexOf(".tar") > 0 || FiletoUpload.indexOf(".zip") > 0 || FiletoUpload.indexOf(".sit") > 0 || FiletoUpload.indexOf(".sitx") > 0)
       		{
	       		//CheckResult=CompareFiles.check(ClientPath+"/"+FiletoUpload, ftp.retrieveFileStream(FiletoUpload));	//For PC
       		}
	       	ftp.changeToParentDirectory();
	       	if(CheckResult==1)
       		{
	       		throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
       		}
		} catch(Exception e)
		{
			if(e.getMessage().indexOf("problem file is")>0)
				throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
               SOP("The exection in function="+e);
		}
	      		//disConnect();
	   			return fileUploaded;
   	}

/****************************** Upload folder on server **************/
public boolean UploadFolder(String ServerPath, String ClientPath, String FolderName) throws Exception
	{
        SOP("ServerPath="+ServerPath);
        SOP("ClientPath="+ClientPath);
        SOP("FoldertoUpload="+FolderName);

        boolean fileUploaded = false;
        try
            {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(ServerPath);
            SOP("ftp.getStatus() :: "+ftp.getStatus());
            SOP("ftp.printWorkingDirectory() :: "+ftp.printWorkingDirectory());
            //if ( !ftp.changeWorkingDirectory( FolderName ) )
            //    {
            //    ftp.makeDirectory( FolderName );
            //    ftp.changeWorkingDirectory(FolderName);
            //    }

            File inputFolder = new File(ClientPath+"/"+FolderName);
            UploadFolder(inputFolder, ServerPath);

            } catch(Exception e)
		{
                System.out.println("The exection in function="+e);
                throw new Exception("Folder upload fails. Error is -> "+e.getMessage());
    		}
	return fileUploaded;
   	}
    //******************** Upload folder recursive

        public boolean UploadFolder(File sourceFolder, String DestinationFolder) throws Exception
            {
            boolean UploadCompleted = false;
            SOP("sourceFolder == "+sourceFolder.getName());
            SOP("DestinationFolder == "+DestinationFolder);
            try
                {
                ftp.changeWorkingDirectory(DestinationFolder);
                String FolderName = sourceFolder.getName();
                DestinationFolder = DestinationFolder+"/"+FolderName;
                if ( !ftp.changeWorkingDirectory( FolderName ) )
                    {
                    ftp.makeDirectory( FolderName );
                    ftp.changeWorkingDirectory(FolderName);
                    }
                if(sourceFolder.isDirectory())
                    {
                    File[] files = sourceFolder.listFiles();
                    if ( files != null && files.length > 0 )
                        {
                        for ( int i = 0; i < files.length; i++ )
                             {
                                 if ( files[i].isDirectory() )
                                    {
                                     UploadFolder( files[i], DestinationFolder );
                                    }
                                 else
                                    {
                                     if(files[i]!=null && !files[i].getName().equalsIgnoreCase("null")){
                                        FileInputStream sourceFileStream = null;
                                        sourceFileStream = new FileInputStream( files[i] );
                                        ftp.storeFile( files[i].getName(), sourceFileStream );
                                        sourceFileStream.close();
                                     }
                                    }
                             }

                        }
                    }
                }
            catch(Exception e)
		{
                System.out.println("The exection in function="+e);
                throw new Exception("Folder upload fails. Error is -> "+e.getMessage());
    		}
            // Step back up a directory once we're done with the contents of this one.
            try
                {
                 ftp.changeToParentDirectory();
                }
             catch ( IOException e )
                {
                 throw new Exception( "IOException caught while attempting to step up to parent directory"
                         + " after successfully processing " + sourceFolder.getAbsolutePath(),
                         e );
                }

            return UploadCompleted;
            }

   /**************************END******************************/

public boolean downloadFolder(String ServerPath, String clientPath, String FoldertoDownload) throws Exception
{
		String TempServerDirPath=ServerPath;
		String TempclientDirPath=clientPath;
		SOP("downloadFolder :: "+FoldertoDownload);
		SOP("ServerPath :: "+ServerPath);
		SOP("clientPath :: "+clientPath);
		boolean downloaded = false;

		try{
				downloaded = DownloadFolderDetails(ServerPath, clientPath, FoldertoDownload);
			}catch(Exception ex )
			{
				SOP("Exception in DownloadFolder: "+ex.getMessage());
				//throw new Exception(ex.getMessage());
			}
		clientPath=TempclientDirPath;
		ServerPath=TempServerDirPath;

		disConnect();
		return downloaded;

}//Download folder

// /**********************************************************/
public boolean DownloadFolderDetails(String ServerPath, String clientPath, String FoldertoDownload) throws Exception
{
	SOP("downloadFolder222 :: "+FoldertoDownload);
	SOP("ServerPath222 :: "+ServerPath);
	SOP("clientPath222 :: "+clientPath);
	boolean downloaded = false;
	if(FoldertoDownload!=null && !FoldertoDownload.equalsIgnoreCase(""))
	{
		ServerPath=ServerPath+"/"+FoldertoDownload;
		clientPath=clientPath+"/"+FoldertoDownload;
	}

	String FileSeq="";
	try
	{
		ArrayList StageFiles;
		StageFiles = listAllforFolder(ServerPath);

		if(StageFiles.size() > 0)
		{
			SOP("StageFiles.size()==="+StageFiles.size());

				if(!new File(clientPath).exists())
				{
					new File(clientPath).mkdirs();
				}
		}
		//SOP("StageFiles.length==="+StageFiles.length);
		if(StageFiles.size() > 0)
		{
			for(int a=0; a < StageFiles.size(); a++)
			{
				if(StageFiles.get(a)==null)
					continue;

				FileSeq = StageFiles.get(a)+"";

				//change by kavita for versions folder
				if(!FileSeq.equalsIgnoreCase("versions") &&  !FileSeq.equalsIgnoreCase(".rsrc")){
					if(FileSeq.indexOf(".") <= 0)
					{
						DownloadFolderDetails(ServerPath, clientPath, FileSeq);
					}
					else
					{
						File outPutFile = new File(clientPath+"/"+FileSeq);
						if(outPutFile.exists())
						{
							if(outPutFile.delete())
								SOP("Deleted file :: "+outPutFile.getName());
							else
								SOP("Not able to deleted file :: "+outPutFile.getName());
						}
						outPutFile=null;
						downloadFileForFolder(ServerPath, clientPath, FileSeq);
					}
				}
			}///For loop ends
			downloaded = true;
		}
	}catch(Exception ex )
	{
		SOP("Exception in DownloadFolder recursion: "+ex.getMessage());
		downloaded = false;
				//throw new Exception(ex.getMessage());
	}
	return downloaded;
}
// ******************************************************************
public String[] listNew(String ServerPath) throws Exception
{
		SOP("ServerPath::" + ServerPath);
		String ReadNewList[] = null;
		ArrayList readLineArray = null;
		int i=0;
		try
		{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);

                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
        	{
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }

            readLineArray = new ArrayList();

            for( int j = 0; j < size; j++ )
        	{
                SOP("ftpFiles"+j+"=="+ftpFiles[j]);
            	if(ftpFiles[j] != null && ftpFiles[j].isFile())
            		readLineArray.add(ftpFiles[j].getName()+"");
        	}

            ReadNewList = new String[readLineArray.size()];
            for( int j = 0; j < readLineArray.size(); j++ )
    		{
            	ReadNewList[j]=readLineArray.get(j)+"";
    		}
		}catch(Exception ex){
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());
		}finally{
			disConnect();
		}
		return ReadNewList;
	}

//	 ******************************************************************
public String[] listFileCTC(String ServerPath) throws Exception
{
		String ReadNewList[] = null;
		ArrayList readLineArray = null;
		int i=0;
		try
		{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
        	{
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }

            readLineArray = new ArrayList();

            for( int j = 0; j < size; j++ )
        	{
            	if(ftpFiles[j].isFile()){
            		readLineArray.add(ftpFiles[j].getName()+"");
            		SOP("file name in ftp :: " +ftpFiles[j].getName()+"");
            	}
        	}

            ReadNewList = new String[readLineArray.size()];
            for( int j = 0; j < readLineArray.size(); j++ )
    		{
            	ReadNewList[j]=readLineArray.get(j)+"";
    		}
			//
		}catch(Exception ex){
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		return ReadNewList;
	}

	// ******************************************************************
	public boolean DeleteFile(String ServerPath, String FiletoDelete) throws Exception
	{
		boolean FileDeleted = false;
		try
		{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
            {
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }

            for( int j = 0; j < size; j++ )
            {
            	if(ftpFiles[j].isFile())
            	{
            		String tempFile =ftpFiles[j].getName()+"";
            		if(tempFile.equals(FiletoDelete))
            		{
            			FileDeleted = ftp.deleteFile(FiletoDelete);
            			SOP("Inside file deletion method :: " + FileDeleted);
            		}
            	}
            }
		}catch(Exception ex){
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());

		}finally{
			disConnect();
		}
		return FileDeleted;
	}
	// ******************************************************************
	public boolean DeleteDirectory(String ServerPath) throws Exception
	{
		boolean FileDeleted = false;
		try
		{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
            {
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }
            for( int j = 0; j < size; j++ )
            {
                    if(ftpFiles[j].isDirectory())
                        {
                        SOP("Directory ::  "+ServerPath+"/"+ftpFiles[j].getName());
                        SOP("Group ::  "+ftpFiles[j].getGroup());
                        SOP("Listing ::  "+ftpFiles[j].getRawListing());
                        DeleteDirectory(ServerPath+"/"+ftpFiles[j].getName()+"");
                        }
                    else
                        {
                        SOP("Directory ::  "+ServerPath+"/"+ftpFiles[j].getName());
                        SOP("Group ::  "+ftpFiles[j].getGroup());
                        SOP("Listing ::  "+ftpFiles[j].getRawListing());
        		String tempFile =ftpFiles[j].getName();
    			FileDeleted = ftp.deleteFile(tempFile);
    			SOP("Inside file deletion method :: " + FileDeleted);
                        }
            }

            String folder = ServerPath.substring(ServerPath.lastIndexOf("/")+1);
            String ServerPath1 = ServerPath.substring(0,ServerPath.lastIndexOf("/"));

            //ftp.changeWorkingDirectory(ServerPath1);
	    //FTPFile[] ftpFiles1 = ftp.listFiles();2222222

            boolean changeDir1 = ftp.changeWorkingDirectory(ServerPath1);
            FTPFile[] ftpFiles1 = null;
            if(changeDir1)
                {
                ftpFiles1 = ftp.listFiles();
                }
            else
                {
                throw new Exception("Error in change directory for - "+ServerPath1);
                }

			int size1 = ( ftpFiles1 == null ) ? 0 : ftpFiles1.length;
			for( int j = 0; j < size1; j++ )
            {
				if(ftpFiles1[j].isDirectory())
            	{
					String tempDir = ftpFiles1[j].getName();
	        		if(tempDir.equals(folder))
            		{
	        			FileDeleted = ftp.removeDirectory(ServerPath);
	        			SOP("Inside file directory deletion method :: " + FileDeleted);
	    			}
            	}
            }
		}catch(Exception ex)
		{
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		return FileDeleted;
	}
//	 ******************************************************************
	public boolean RenameFile(String ServerPath, String File2rename, String Renamewith) throws Exception
	{
		boolean Renamed = false;
		try
		{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
        	{
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }

            for( int j = 0; j < size; j++ )
        	{
            	if(ftpFiles[j].isFile())
        		{
                            if (ftpFiles[j] != null && !ftpFiles[j].getName().equalsIgnoreCase("null")) {
                                String tempFile = ftpFiles[j].getName() + "";
                                if (tempFile.equals(File2rename)) {
                                    Renamed = ftp.rename(File2rename, Renamewith);
                                    SOP("Inside file deletion method :: " + Renamed);
                                }
                            }
        		}
        	}
		}catch(Exception ex)
		{
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());
		}finally{
			disConnect();
		}
		return Renamed;
	}

//	 ******************************************************************
// Added by Atul 0n 11th April 2009 to get file timestamp on server
	public Calendar GetFileTimeStamp(String ServerPath, String FileName4TimeStamp) throws Exception
	{
		Calendar FileTimeStamp=null;
		try
		{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
        	{
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }

            for( int j = 0; j < size; j++ )
        	{
            	if(ftpFiles[j].isFile())
        		{
            		String tempFile =ftpFiles[j].getName()+"";
            		if(tempFile.equals(FileName4TimeStamp))
        			{
            			FileTimeStamp = ftpFiles[j].getTimestamp();
        			}
        		}
        	}
		}catch(Exception ex)
		{
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());

		}finally{
			//disConnect();
		}
		return FileTimeStamp;
	}

///******************************************************************
	public ArrayList listAll(String ServerPath) throws Exception
	{
		ArrayList readLineArray = null;
		int i=0;
		try{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
        	{
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }

            readLineArray = new ArrayList();

            for( int j = 0; j < size; j++ )
        	{
                 readLineArray.add(ftpFiles[j].getName()+"");
        	}
		}catch(Exception ex)
		{
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());
		}finally{
			disConnect();
		}
			return readLineArray;
	}
	/**********************************************************/
	public ArrayList listAllforFolder(String ServerPath) throws Exception
	{
		ArrayList readLineArray = null;
		int i=0;
		try
		{
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

                FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;
			SOP("ServerPath is " + ServerPath);
			SOP("ftpFiles.length " + ftpFiles.length);
			SOP("size is " + size);
            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
        	{
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }

            readLineArray = new ArrayList();

            for( int j = 0; j < size; j++ )
        	{
                 readLineArray.add(ftpFiles[j].getName()+"");
        	}

			//
		}catch(Exception ex)
		{
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());
		}
		SOP("in listAllforFolder printWorkingDirectory----"+ftp.printWorkingDirectory());
			return readLineArray;
	}
/**********************************************************/
	public String[] listAllFilesIncludingDirAlso(String ServerPath) throws Exception
	{
		String readLineArray[] = null;
		int i=0;
		try{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
        	{
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }

            readLineArray = new String[size];

            for( int j = 0; j < size; j++ )
        	{
                 readLineArray[j]=ftpFiles[j].getName()+"";
        	}
		}catch(Exception ex)
		{
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());

		}finally{
			disConnect();
		}
			return readLineArray;
	}
	/**********************************************************/
	public ArrayList listDirectory(String ServerPath) throws Exception
	{
		ArrayList readLineArray = null;
		int i=0;
		try{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
        	{
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }

            readLineArray = new ArrayList();

            for( int j = 0; j < size; j++ )
        	{
            	if(ftpFiles[j].isDirectory())
            		readLineArray.add(ftpFiles[j].getName());
        	}
		}catch(Exception ex){
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());

		}finally{
			disConnect();
		}
			return readLineArray;
	}
	/**********************************************************/

	public  void filecopy(String inFile, String outFile)
    {
        int n;
        int reply;
        SOP("Hi  file copy fucntion");
        ArrayList arsecond = new ArrayList();
        try
        {
            File outputFile = new File(outFile);
            FTPFile inputFile = new FTPFile();
            inputFile.setRawListing(inFile);
        //   boolean success = outFile.mkdir();//target directory wher U want to place
             boolean success = (new File(outFile)).mkdirs();
             SOP("file copy function is directory created.." + success);
                    File dir = new File(inFile);//your input directory which you want to replicate
                    FTPFile[] children = null;
                    children = ftp.listFiles();

                    String   tempfile="";

                    if (children == null)
                    {
                    	SOP("the children is null");
                    }
                    else
                    {
                        for (int i=0; i<children.length; i++)
                        {
                            // String filename = children[i].getName();
                        //  arsecond.add(chi    ldren[i].getName());
                        	SOP("hi file copy  i am called in file======"+children[i].getName());
                        }
                    }
                    String temp="";
                    for(int p=0;p<children.length;p++)
                    {
                        if(!children[p].isDirectory())
                        {
                            temp = children[p].getName();
                            arsecond.add(children[p].getName());
                             Iterator itsecond = arsecond.iterator();

                            while(itsecond.hasNext())
                        	{
                                String childfile = (String) itsecond.next();
                                SOP("child file is" +childfile );
                                if(children[p].isFile())
                                {
                                	SOP("outputchild is " +outputFile );
                                	boolean retValue = ftp.retrieveFile(childfile, new FileOutputStream(outputFile+"\\"+ childfile));

	                                if (!retValue)
	                                {
	                                  throw new Exception ("Downloading of remote file "+ inputFile+" failed. ftp.retrieveFile() returned false.");
	                                }
                                }
                        	}
                            tempfile = outputFile+"/";
                            SOP("Inside if File copy temp is" +temp );
                        }
                        else
                        {
                            //temp = extractDirName(children[p].toString());
                            temp = children[p].getName();
                            tempfile = outputFile+"/" +temp ;
                            SOP("Inside else  File copy temp is" +temp );
                        }
                        //File f = new File(outputFile+"/"+temp);
                        //SOP("\n out file ="+f);
                    File f = new File(tempfile);
                    SOP("inside file copy verfy  f" + f);
                    SOP("inside file copy verfy  temp" + temp) ;

                    copyFile(f,temp+"");
                }
        	}
        catch(Exception smbe)
        {
        	SOP("The smbFile fileCopy function ="+smbe);
        }
    }

/**********************************************************/
	public InputStream getInputStream(String productionServerFilePath, String FileName) throws Exception
	{
		InputStream Istream = null;
		try{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.changeWorkingDirectory(productionServerFilePath);

			Istream = ftp.retrieveFileStream(FileName);
			}catch(Exception ex){
				SOP("Problem in getting Input Stream :::"+ex.getMessage());
				throw ex;

			}
			return Istream;
	}

	/**********************************************************/

public ArrayList getZipFileList(String productionServerFilePath, String ZipFileName) throws Exception
{
		InputStream Istream = null;
		ArrayList fileListInsideZip = new ArrayList();
		try
		{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			ftp.changeWorkingDirectory(productionServerFilePath);

			SOP("printWorkingDirectory--after--->"+ftp.printWorkingDirectory());
			Istream = ftp.retrieveFileStream(ZipFileName);

		}catch(Exception ex)
		{
			SOP("Problem in getting Input Stream :::"+ex.getMessage());
		}

		try
		{
			if(Istream!=null)
			{
				ZipInputStream zipInStream = new ZipInputStream(Istream);
				ZipEntry zipentry=null;
				while((zipentry=zipInStream.getNextEntry())!=null)
				{
					String FileNmaer=zipentry.getName();
					SOP("FileNmaer========="+FileNmaer);
					fileListInsideZip.add(FileNmaer);
					zipInStream.closeEntry();
				}
				zipInStream.close();
			}
			Istream.close();
			ftp.completePendingCommand();
		}catch(Exception ex)
		{
			SOP("Problem in getting Input Stream :::"+ex.getMessage());
		}
		return fileListInsideZip;
	}

///************************** Unzip file on server
public boolean unZipFile(String productionServerFilePath, String ZipFileName) throws Exception
{
		InputStream Istream = null;
		boolean unZipCompleted = false;
		try
		{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			ftp.changeWorkingDirectory(productionServerFilePath);

			SOP("printWorkingDirectory--after--->"+ftp.printWorkingDirectory());
			Istream = ftp.retrieveFileStream(ZipFileName);

		}catch(Exception ex)
		{
			SOP("Problem in getting Input Stream :::"+ex.getMessage());
		}

		try
		{
			if(Istream!=null)
			{
				ZipInputStream zipInStream = new ZipInputStream(Istream);
				ZipEntry zipentry=null;
				while((zipentry=zipInStream.getNextEntry())!=null)
				{
					String FileNmaer=zipentry.getName();
					SOP("FileNmaer========="+FileNmaer);
                                        if(FileNmaer.endsWith("/"))
                                            FileNmaer=FileNmaer.substring(0, FileNmaer.length()-1);
                                        SOP("FileNmaer2222222222========="+FileNmaer);
                                        InputStream iss =new ByteArrayInputStream(zipentry.getExtra());
                                        ApacheFtp   AFP = new ApacheFtp ("192.168.57.87", "pmpi", "pmpi@123");
                                        AFP.UploadFile(productionServerFilePath, FileNmaer, iss);//.storeFile(FileNmaer, iss);
                                        iss.close();
				}
				zipInStream.close();
			}
			Istream.close();
			ftp.completePendingCommand();
                        unZipCompleted = true;
		}catch(Exception ex)
		{
			SOP("Problem in getting Input Stream :::"+ex.getMessage());
                        unZipCompleted=false;
		}
		return unZipCompleted;
	}

///**************************Download Specific folder Expect specific folder********************************/
public boolean DownloadFolderExpectSpecficFolder(String ServerPath, String clientPath, String FoldertoDownload,String notDownloadFolder) throws Exception
{
	SOP("DownloadFolderExpectSpecficFolder222 :: "+FoldertoDownload);
	SOP("ServerPath222 :: "+ServerPath);
	SOP("clientPath222 :: "+clientPath);
	boolean downloaded = false;
	if(FoldertoDownload!=null && !FoldertoDownload.equalsIgnoreCase(""))
	{
		ServerPath=ServerPath+"/"+FoldertoDownload;
		clientPath=clientPath+"/"+FoldertoDownload;
	}
	String FileName="";
	String FileType ="";
	try
	{
		ArrayList listAllDetail = listforFolderAndFile(ServerPath);

		if(listAllDetail!=null && listAllDetail.size() > 0)
		{
			if(!new File(clientPath).exists())
			{
				new File(clientPath).mkdirs();
			}
		}
		//SOP("StageFiles.length==="+StageFiles.length);
		if(listAllDetail!=null && listAllDetail.size() > 0)
		{
			for(int a=0; a < listAllDetail.size(); a=a+2)
			{
				if(listAllDetail.get(a)==null)
					continue;

				FileName = listAllDetail.get(a)+"";
				FileType = listAllDetail.get(a+1)+"";

				if(FileType!=null && FileType.equals("file"))
				{
					File outPutFile = new File(clientPath+"/"+FileName);
					if(outPutFile.exists())
					{
						if(outPutFile.delete())
							SOP("Deleted file :: "+outPutFile.getName());
						else
							SOP("Not able to deleted file :: "+outPutFile.getName());
					}
					outPutFile=null;
					downloadFileForFolder(ServerPath, clientPath, FileName);
				}else if(FileType!=null && FileType.equals("dir"))
				{
					if(!FileName.equals(notDownloadFolder) && !FileName.equals("versions")&& !FileName.equals(".rsrc"))
					{
						DownloadFolderExpectSpecficFolder(ServerPath, clientPath, FileName,"art");
					}
				}
			}///For loop ends
			downloaded = true;
		}
	}catch(Exception ex )
	{
		SOP("Exception in DownloadFolderExpectSpecficFolder recursion: "+ex.getMessage());
		downloaded = false;
				//throw new Exception(ex.getMessage());
	}finally{
		disConnect();
	}
	return downloaded;
}
/**********************************************************/
public ArrayList listforFolderAndFile(String ServerPath) throws Exception
{
	ArrayList listAllDetail = null;
	int i=0;
	try{
		ftp.setFileType(FTP.BINARY_FILE_TYPE);

		FTPFile ftpfile = new FTPFile();
        ftpfile.setRawListing(ServerPath);

        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
        FTPFile[] ftpFiles = null;
        if(changeDir)
            {
            ftpFiles = ftp.listFiles();
            }
        else
            {
            throw new Exception("Error in change directory for - "+ServerPath);
            }

		int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

		SOP("ftpFiles.length " + ftpFiles.length);
        int reply = ftp.getReplyCode();
        SOP("reply code" + reply);
        if(!FTPReply.isPositiveCompletion(reply))
    	{
            throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
        }

        listAllDetail = new ArrayList();
        for( int j = 0; j < size; j++ )
    	{
        	listAllDetail.add(ftpFiles[j].getName()+"");
        	if(ftpFiles[j].isDirectory())
        		listAllDetail.add("dir");
        	else
        		listAllDetail.add("file");
    	}
	}catch(Exception ex)
	{
		SOP("Error in listforFolderAndFile :: " +ex.getMessage());
		throw new Exception(ex.getMessage());
	}
	return listAllDetail;
}
	/**********************************************************/
/**************************Changing for communication flow done by kavita********************************/
public boolean UploadFile_CF(String ServerPath,  String FiletoUpload,FileInputStream FileUpload) throws Exception
   		{
		boolean fileUploaded = false;
SOP("ServerPath="+ServerPath);
SOP("FiletoUpload="+FiletoUpload);
          try
	      	{
           ftp.setFileType(FTP.BINARY_FILE_TYPE);
           ftp.changeWorkingDirectory(ServerPath);

			boolean uplValue=false;

			uplValue = ftp.storeFile(FiletoUpload, FileUpload);
			fileUploaded = uplValue;

	        if (!uplValue)
	        	{
	            throw new Exception ("Unable to upload file "+ FiletoUpload+" failed. ftp.storeFile() returned false.");
	            }

            int CheckResult = 0;
	       	if(FiletoUpload.indexOf(".tar") > 0 || FiletoUpload.indexOf(".zip") > 0 || FiletoUpload.indexOf(".sit") > 0 || FiletoUpload.indexOf(".sitx") > 0 || FiletoUpload.indexOf(".eps") > 0)
	       		{
	       		//CheckResult=CompareFiles.check(ClientPath+"/"+FiletoUpload, ftp.retrieveFileStream(FiletoUpload));	//For PC
	       		}
	       	if(CheckResult==1)
	       		{
	       		throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
	       		}

	   			} catch(Exception e)
	   					{
	   				if(e.getMessage().indexOf("problem file is")>0)
	   					throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
	                   SOP("The exection in function="+e);
	   					}
	      		disConnect();
	      		return fileUploaded;
	   	}
/**************** New method by Atul - to upload a file with InputStream  ********************************/
public boolean UploadFile(String ServerPath,  String FiletoUpload,InputStream FileUpload) throws Exception
   		{
		boolean fileUploaded = false;
SOP("ServerPath="+ServerPath);
SOP("FiletoUpload="+FiletoUpload);
          try
	      	{
           ftp.setFileType(FTP.BINARY_FILE_TYPE);
           ftp.changeWorkingDirectory(ServerPath);

			boolean uplValue=false;

			uplValue = ftp.storeFile(FiletoUpload, FileUpload);
			fileUploaded = uplValue;

	        if (!uplValue)
	        	{
	            throw new Exception ("Unable to upload file "+ FiletoUpload+" failed. ftp.storeFile() returned false.");
	            }

            int CheckResult = 0;
	       	if(FiletoUpload.indexOf(".tar") > 0 || FiletoUpload.indexOf(".zip") > 0 || FiletoUpload.indexOf(".sit") > 0 || FiletoUpload.indexOf(".sitx") > 0 || FiletoUpload.indexOf(".eps") > 0)
	       		{
	       		//CheckResult=CompareFiles.check(ClientPath+"/"+FiletoUpload, ftp.retrieveFileStream(FiletoUpload));	//For PC
	       		}
	       	if(CheckResult==1)
	       		{
	       		throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
	       		}

	   			} catch(Exception e)
	   					{
	   				if(e.getMessage().indexOf("problem file is")>0)
	   					throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
	                   SOP("The exection in function="+e);
	   					}
	      		disConnect();
	      		return fileUploaded;
	   	}
/**********************************************************/
/**************** New method by Atul - to upload a file with InputStream  ********************************/
public boolean UploadFileGlobal(String ServerPath,  String FiletoUpload,InputStream FileUpload) throws Exception
   		{
		boolean fileUploaded = false;
SOP("ServerPath="+ServerPath);
SOP("FiletoUpload="+FiletoUpload);
          try
	      	{
           ftp.setFileType(FTP.BINARY_FILE_TYPE);
           ftp.changeWorkingDirectory(ServerPath);

			boolean uplValue=false;

			uplValue = ftp.storeFile(FiletoUpload, FileUpload);
			fileUploaded = uplValue;

	        if (!uplValue)
	        	{
	            throw new Exception ("Unable to upload file "+ FiletoUpload+" failed. ftp.storeFile() returned false.");
	            }

            int CheckResult = 0;
	       	if(FiletoUpload.indexOf(".tar") > 0 || FiletoUpload.indexOf(".zip") > 0 || FiletoUpload.indexOf(".sit") > 0 || FiletoUpload.indexOf(".sitx") > 0 || FiletoUpload.indexOf(".eps") > 0)
	       		{
	       		//CheckResult=CompareFiles.check(ClientPath+"/"+FiletoUpload, ftp.retrieveFileStream(FiletoUpload));	//For PC
	       		}
	       	if(CheckResult==1)
	       		{
	       		throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
	       		}

	   			} catch(Exception e)
	   					{
	   				if(e.getMessage().indexOf("problem file is")>0)
	   					throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
	                   SOP("The exection in function="+e);
	   					}
	      		return fileUploaded;
	   	}
/**************** New method by Atul - to chek file on server *****************************/
public boolean CheckFileExist(String ServerPath,  String File2Serch) throws Exception
   		{
		boolean fileFind = false;
SOP("ServerPath="+ServerPath);
SOP("File2Serch="+File2Serch);
          try
	     {
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.changeWorkingDirectory(ServerPath);

            FTPFile[] ftpFiles = ftp.listFiles();

            int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            SOP("Array size is" + size);
            FTPFile ftpfile = new FTPFile();
            ftpfile.setRawListing(ServerPath);

            for( int i = 0; i < size; i++ )
                {
                if(ftpFiles[i]!=null && !ftpFiles[i].getName().equalsIgnoreCase("null")){
                        ftpfile = ftpFiles[i];
                        String TempFileName = ftpFiles[i].getName();
                        if(TempFileName.equals(File2Serch))
                            {
                            fileFind = true;
                            break;
                            }
                    }
        	}
            }catch(Exception e)
                    {
                    fileFind = false;
                    if(e.getMessage().indexOf("problem file is")>0)
                        throw new Exception("Due to connection problem file is not uploaded properly. Please try again...");
	            SOP("The exection in function="+e);
                    }
                disConnect();
      		return fileFind;
	   	}

/***********************************************************/

public boolean CreateFolder(String FolderName, String ServerPath) throws Exception
            {
            boolean FolderCreated = false;
            SOP("FolderName == "+FolderName);
            SOP("ServerPath == "+ServerPath);
            try
                {
                ftp.changeWorkingDirectory(ServerPath);
                if ( !ftp.changeWorkingDirectory( FolderName ) )
                    {
                	
                    ftp.makeDirectory( FolderName );
                    if(ftp.changeWorkingDirectory(FolderName))
                        FolderCreated = true;
                    } else {
                //SOP("in else of CheckFileExist exists== ");
                if (CheckFileExist(ServerPath, FolderName)) {
                    SOP("ftp.CheckFileExist-->" + ServerPath + FolderName);
                    FolderCreated = true;
                }
            }
        } catch (Exception e) {
            System.out.println("The exection in folder creation=" + e);
            throw new Exception("Folder creation fails. Error is -> " + e.getMessage());
        }
        	SOP("FolderCreated @@@== " + FolderCreated);
            return FolderCreated;
    }

/**********************************************************/
public boolean disConnect()
{
	 boolean isDisconnect=false;
	 try
	 {
		 this.ftp.logout();
		 isDisconnect=true;
		 SOP("Disconnected FTP connection ");
	 }catch(Exception ee){ SOP(" Disconnect System has a problem");}
	return isDisconnect;
}
	/**********************************************************/

/*******************List Files (No Space issue) ***************************************/
public String[] listOnlyFiles(String ServerPath) throws Exception
{
		SOP("ServerPath::" + ServerPath);
		String ReadNewList[] = null;
		ArrayList readLineArray = null;
		int i=0;
		try
		{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
        	{
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }

            readLineArray = new ArrayList();

            for( int j = 0; j < size; j++ )
        	{
                if(ftpFiles[j]!=null && !ftpFiles[j].getName().equalsIgnoreCase("null")){
                SOP(ftpFiles[j].getName()+"");
            	if(ftpFiles[j].isFile() && !ftpFiles[j].getName().startsWith("."))
            		readLineArray.add(ftpFiles[j].getName()+"");
        	}
            }

            ReadNewList = new String[readLineArray.size()];

            /*

             if(ftpFiles[j]!=null && !ftpFiles[j].getName().equalsIgnoreCase("null")){
                                if(ftpFiles[j].isDirectory() && !ftpFiles[j].getName().startsWith(".")){
                                    SOP(ftpFiles[j].getName()+"");
                                        readLineArray.add(ftpFiles[j].getName());
                                }

                            }

             */

            for( int j = 0; j < readLineArray.size(); j++ )
    		{
            	ReadNewList[j]=readLineArray.get(j)+"";
    		}

		}catch(Exception ex){
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());
		}finally{
			disConnect();
		}
		return ReadNewList;
	}

/*******************List only directory without closing connection (No Space issue) ***************************************/

public ArrayList listOnlyDirectory(String ServerPath) throws Exception
	{
		ArrayList readLineArray = null;
		int i=0;
		try{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			FTPFile ftpfile = new FTPFile();
	        ftpfile.setRawListing(ServerPath);

	        boolean changeDir = ftp.changeWorkingDirectory(ServerPath);
                FTPFile[] ftpFiles = null;
                if(changeDir)
                    {
                    ftpFiles = ftp.listFiles();
                    }
                else
                    {
                    throw new Exception("Error in change directory for - "+ServerPath);
                    }

			int size = ( ftpFiles == null ) ? 0 : ftpFiles.length;

            int reply = ftp.getReplyCode();
            SOP("reply code" + reply);
            if(!FTPReply.isPositiveCompletion(reply))
        	{
                throw new Exception ("Unable to change working directory " + "to:"+ServerPath);
            }

            readLineArray = new ArrayList();
            SOP("size -" + size);
                    for( int j = 0; j < size; j++ )
                        {
                            if(ftpFiles[j]!=null && !ftpFiles[j].getName().equalsIgnoreCase("null")){
                                if(ftpFiles[j].isDirectory() && !ftpFiles[j].getName().startsWith(".")){
                                    SOP(ftpFiles[j].getName()+"");
                                        readLineArray.add(ftpFiles[j].getName());
                                }

                            }
                        }
		}catch(Exception ex){
                    ex.printStackTrace();
			SOP("Error :: " +ex.getMessage());
			throw new Exception(ex.getMessage());

		}finally{
			//disConnect();
		}
			return readLineArray;
	}
public void DeleteFtpDirectries(FTPClient ftp, String remotePath)throws Exception
{
    // change the directory
    ftp.changeWorkingDirectory(remotePath);

    // delete all files from the remotePath directory
    FTPFile[] files = ftp.listFiles();
    for (int i = 0; i < files.length; i++)
    {
        if (files[i].isFile())
        {
            SOP("Deleting file: "+ files[i].getName());

            ftp.deleteFile(files[i].getName());
        }
    }

    // delete all subdirectories in the remotePath directory
    for (int i = 0; i < files.length; i++)
    {
        if (files[i].isDirectory())
            DeleteFtpDirectries(ftp, files[i].getName());
    }

    // delete this directory
    ftp.changeWorkingDirectory("..");
    SOP("Deleting Directory: "+ remotePath);
    ftp.removeDirectory(remotePath);
}
   public void ftpCreateDirectoryTree( FTPClient client, String landingPath,String dirTree ) throws IOException {
	   System.out.println("inside ftpCreateDirectoryTree...........");
      boolean dirExists = true;
      System.out.println("root changed for landingPath:"+client.changeWorkingDirectory(landingPath));

      String[] directories = dirTree.split("/");
      for (String dir : directories ) {
           SOP("dir::"+dir);
        if (dir.trim().length()>0) {
          if (dirExists) {
        	  
            dirExists = client.changeWorkingDirectory(dir);
            SOP("dir created::"+dirExists);
          }
          if (!dirExists) {
            if (!client.makeDirectory(dir)) {
              throw new IOException("Unable to create remote directory '" + dir + "'.  error='" + client.getReplyString()+"'");
            }
            if (!client.changeWorkingDirectory(dir)) {
              throw new IOException("Unable to change into newly created remote directory '" + dir + "'.  error='" + client.getReplyString()+"'");
            }
          }
        }
      }
}

/**********************************************************/
    public static void main(String[] args) throws  Exception
    {
    	//ApacheFtp   upload = new ApacheFtp ("192.168.180.192", "brokermer", "dejavu880");
        /*ApacheFtp   upload = new ApacheFtp ("192.168.180.17", "brokerura", "uXg9q5S0");
        ApacheFtp   upload2 = new ApacheFtp ("192.168.180.17", "brokerura", "uXg9q5S0");
        ApacheFtp   upload3 = new ApacheFtp ("192.168.180.17", "brokerura", "uXg9q5S0");


        //ApacheFtp   upload = new ApacheFtp ("192.168.57.87", "pmpi", "pmpi@123");

    	//upload.CreateFolder("TestFolder", "/ura3/PowerManage/dev");
        //upload.DeleteDirectory("/PMPI/adfmtest");
        //upload.DeleteDirectory("/PMPI/adfmtest/JWVC008A-01");
        InputStream ist = upload.getInputStream("/ura4/PowerManage/dev/", "CCR-10-1260_LR.pdf");
        InputStream ist2 = upload3.getInputStream("/ura4/PowerManage/dev/", "CCR-10-1260_LR.pdf");
        upload2.UploadFileWithCheckSum("/ura3/PowerManage/dev/", "CCR-10-1260_LR.pdf", ist, ist2);
        ist.close();
        ist2.close();*/
        ApacheFtp upload3 = new ApacheFtp("192.168.180.152", "col", "123456");
        boolean isEixt = upload3.CheckFileExist("/102/es/CTB00123/work/indd/Test Books/Grade 6/Math/d2189502_16ma_s12GA", "d2189502_16MA_s12GA.indd");
        System.out.println("isEixt===" + isEixt);
        //upload3.RenameFile(null, null, null);
    }
    int ShowSOP = 1;
	public void SOP(String message2print)
		{
		if(ShowSOP == 1)
			System.out.println(message2print);
		}
}
