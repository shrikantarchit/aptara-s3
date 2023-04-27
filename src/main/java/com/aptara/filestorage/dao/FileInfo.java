package com.aptara.filestorage.dao;

public class FileInfo {

	String sno;
	String fileName;
	long size;
	public String getSno() {
		return sno;
	}
	public void setSno(String sno) {
		this.sno = sno;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	@Override
	public String toString() {
		return "FileInfo [sno=" + sno + ", fileName=" + fileName + ", size=" + size + "]";
	}
	public FileInfo(String sno, String fileName, long size) {
		super();
		this.sno = sno;
		this.fileName = fileName;
		this.size = size;
	}
	public FileInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
