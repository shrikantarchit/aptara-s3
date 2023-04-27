package com.aptara.filestorage.ftp;

import java.util.Comparator;

import org.apache.commons.net.ftp.FTPFile;

public class LastModifiedComparator implements Comparator<FTPFile>  {
	public int compare(FTPFile f1, FTPFile f2) {
        return f1.getTimestamp().compareTo(f2.getTimestamp());
    }
}



