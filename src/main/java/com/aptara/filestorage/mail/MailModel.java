package com.aptara.filestorage.mail;

public class MailModel {

	private String mailfrom;
	private String mailto;
	private String mailcc;
	private String mailbcc;
	private String remark;
	private String url;//URL;
	private String project_title;//PROJECT_TITLE;
	private String mailhost;//MAILHOST;
	private int isAWS;
	private int mailsupport;//  MAILSUPPORT
	
	public String getMailfrom() {
		return mailfrom;
	}
	public void setMailfrom(String mailfrom) {
		this.mailfrom = mailfrom;
	}
	public String getMailto() {
		return mailto;
	}
	public void setMailto(String mailto) {
		this.mailto = mailto;
	}
	public String getMailcc() {
		return mailcc;
	}
	public void setMailcc(String mailcc) {
		this.mailcc = mailcc;
	}
	public String getMailbcc() {
		return mailbcc;
	}
	public void setMailbcc(String mailbcc) {
		this.mailbcc = mailbcc;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getProject_title() {
		return project_title;
	}
	public void setProject_title(String project_title) {
		this.project_title = project_title;
	}
	public String getMailhost() {
		return mailhost;
	}
	public void setMailhost(String mailhost) {
		this.mailhost = mailhost;
	}
	public int getIsAWS() {
		return isAWS;
	}
	public void setIsAWS(int isAWS) {
		this.isAWS = isAWS;
	}
		
}
