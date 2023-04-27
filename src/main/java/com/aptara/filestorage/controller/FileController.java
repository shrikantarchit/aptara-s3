package com.aptara.filestorage.controller;

import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.Upload;
import com.aptara.filestorage.config.StorageConfig;
import com.aptara.filestorage.connectionpool.ConnectionObject;
import com.aptara.filestorage.dao.FileInfo;
import com.aptara.filestorage.ftp.ApacheSFtp;
import com.aptara.filestorage.mail.Mail;

@Controller
@RequestMapping("/contra")
public class FileController {
	
	//private static String MAIL_FROM = "acmdev@aptaracorp.com";
	private static String MAIL_FROM = "tapsadmin@aptaracorp.awsapps.com";
	//private static String MAIL_TO = "taps@aptaracorp.com";// 
																		 
	private static String MAIL_CC = "tapssupport@aptaracorp.com, anil.tyagi@aptaracorp.com, Sehar.Tahir@aptaracorp.com,taps@aptaracorp.com";
	private static String MAIL_BCC ="archit.shrikant@aptaracorp.com";
	@Autowired
	StorageController storageController;
	String user;
	static String name = "";
	static String jobid = "";
	static String itemid = "";
	static String usr = "";
	static String cref = "";
	static String stage = "";
	static String taskid = "";
	static String journal = "";
	static String acronym = "";
	static String paperid = "";
	
	FileController() {
		
	}
	
	FileController(String name, String jobid, String itemid, String usr, String cref, String stage, String taskid,String journal) {
		this.name = name;
		this.jobid = jobid;
		this.itemid = itemid;
		this.usr = usr;
		this.cref = cref;
		this.stage = stage;
		this.taskid = taskid;
		this.journal = journal;
	 }
	public FileController(String name, String acronym, String paperid) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.acronym = acronym;
		this.paperid = paperid;
		System.out.println(this.name+this.acronym+this.paperid);
		}
	
	@RequestMapping("/uploadPage")
	public ModelAndView  getPage(HttpServletRequest request) {
		  String name = request.getParameter("name");
	       String acronym = request.getParameter("acronym");
	       String paperid = request.getParameter("paperid");
	  
	   //    System.out.println("inside  >>"+name+acronym+paperid);
		//System.out.println(this.name+this.acronym+this.paperid);
		 
		 ModelAndView modelAndView = new ModelAndView();
	        modelAndView.setViewName("AuthorDashboard");
	         modelAndView.addObject("name", name);
	         modelAndView.addObject("acronym", acronym);
	         modelAndView.addObject("paperid", paperid);
	         this.name=null;
	         this.acronym=null;
	         this.paperid=null;
	         return modelAndView;
	 }
	@RequestMapping("/homepage")
	public ModelAndView home() {	
		System.out.println("inside homepage");
		// Model theModel=new Model();
		 ModelAndView theModel = new ModelAndView();
		theModel.setViewName("test");
		theModel.addObject("username", this.name);
		theModel.addObject("jobid", this.jobid);
		theModel.addObject("itemid", this.itemid);
		theModel.addObject("cref", this.cref);
		theModel.addObject("usr", this.usr);
		theModel.addObject("stage", this.stage);
		theModel.addObject("taskid", this.taskid);
		theModel.addObject("journal", this.journal);
		return theModel;
	}

	@RequestMapping(value = { "/fileupload" }, consumes = { "multipart/form-data" })
	public String fileupload(@RequestParam(value = "file") MultipartFile file,Model theModel) {
		 System.out.println(file.getOriginalFilename());
		try {
			if(!file.getOriginalFilename().isEmpty()) {
			// storageController.uploadFile(file);
			storageController.uploadFile(user, file);
			return "redirect:/contra/list";
			}
			else {
				System.out.println("no file selected");
				theModel.addAttribute("msg","Please chose a valid file");
				return "index";
				}
		} catch (Exception e) {
			return null;
		}
	}
	@SuppressWarnings("unused")
	@RequestMapping(value = { "/authorFileupload" }, consumes = { "multipart/form-data" })
	public ModelAndView authorFileupload(HttpServletRequest request,@RequestParam(value = "file") MultipartFile file,Model theModel) {
		 System.out.println("name >>"+file.getOriginalFilename()+"   size >>"+file.getSize()+"bytes");
		 String name = request.getParameter("name");
		 
			boolean uplflg= false;
			boolean renameflg= false; 
			 String email=request.getParameter("emailId");
			 String mesg=request.getParameter("message");
			 System.out.println("name >>"+name+" emailId >>"+email); 
			 ModelAndView modelAndView = new ModelAndView();   
		try {
			//if(false)
			if(!file.getOriginalFilename().isEmpty()) 
			{
				// FTP server upload starts
				InputStream ins = file.getInputStream();
				ApacheSFtp sourceFtp = null;
				//Live env
				sourceFtp = new ApacheSFtp("ec2-34-197-215-250.compute-1.amazonaws.com", "acmpms", "acm@2017", "");
				//Test env
				//sourceFtp = new ApacheSFtp("acmtech02.ind.aptaracorp.com", "acmpmsdev", "123456", "");
				uplflg=sourceFtp.UploadFile("/ACM_PMS/PMS/dropzone/unprocessAU", file.getOriginalFilename()+"_FTPSynk", ins, "");
				ins = null;
				sourceFtp = null;
				// FTP server upload ends
				System.out.println("File upload method called " + name);
				if(uplflg) {
					//Live env
					sourceFtp = new ApacheSFtp("ec2-34-197-215-250.compute-1.amazonaws.com", "acmpms", "acm@2017", "");
					//Test env
					//sourceFtp = new ApacheSFtp("acmtech02.ind.aptaracorp.com", "acmpmsdev", "123456", "");
					renameflg=sourceFtp.RenameFile("/ACM_PMS/PMS/dropzone/unprocessAU", file.getOriginalFilename()+"_FTPSynk", file.getOriginalFilename(), "");
					Thread.sleep(1000);
					System.out.println("rename flag status>>> "+renameflg);
					if(renameflg) {
						 System.out.println("rename done");
						sendMail(file.getOriginalFilename(),name,email,mesg);
					}
					else {
						 System.out.println("rename not done");
							//sendMail(file.getOriginalFilename(),name,email,mesg);
					}
				}
				System.out.println(file.getOriginalFilename() + " uploaded for " + name + new Date());
				modelAndView.setViewName("success");
				return modelAndView;
			}
			else {
				System.out.println("no file selected");
				modelAndView.setViewName("AuthorDashboard");
				modelAndView.addObject("msg","Please chose a valid file");
				//theModel.addAttribute("msg","Please chose a valid file");
				return modelAndView;
				}
		} catch (Exception e) {
			//sendFailuremail(file.getOriginalFilename(),name,email,mesg);
			System.out.println(file.getOriginalFilename() + " uploading failed for " + name + new Date());
			e.printStackTrace();
			return null;
		}
	}
	@RequestMapping(value = "/getfile", method = RequestMethod.POST)
	public String getFile(HttpServletRequest request, Model model) {
		// storageController.getfiles();
		String job_id = request.getParameter("jobid");
		String itemid = request.getParameter("itemid");
		String usr = request.getParameter("usr");
		String cref = request.getParameter("cref");
		String stage = request.getParameter("stage");
		String taskid = request.getParameter("taskid");
		String journal = request.getParameter("journal");
		String firstname = "";
		String lastname = "";
		
		///////////// db connection//////////
		try {
			CallableStatement callstm = null;
			Connection con = ConnectionObject.getConnection();
			System.out.println("Connection object:: "+con);
			String query = "select firstname,lastname from usermaster where userid=?";
			PreparedStatement ps = null;
			ResultSet rs = null;
			System.out.println("-- --query--- " + query);
			ps = con.prepareStatement(query);
			ps.setString(1, usr);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				firstname = rs.getString("firstname");
				lastname = rs.getString("lastname");
			}
			rs.close();
			ps.close();
			
			System.out.println("db connection made");
		} catch (Exception e) {
			e.printStackTrace();
		}
		String name = firstname;
		FileController fc = new FileController(name, job_id,itemid,usr,cref,stage,taskid,journal);
		System.out.println("User Name::>> " + name);
		model.addAttribute("name", name);
		
		System.out.println(
				"Data from powermanage   itemid::::" + itemid + "::  Job ID " + job_id + "::  usr  ::  " + usr);
		return "index";
	}
	@RequestMapping(value = "/loadData", method = RequestMethod.POST)
	public void getForm(HttpServletRequest request, Model model) {
		// storageController.getfiles();
		String name = request.getParameter("name");
		String acronym = request.getParameter("acronym");
		String paperid = request.getParameter("paperid");
		 
		 
		FileController fc = new FileController(name, acronym,paperid);
		System.out.println("User Name::>> " + name);
		model.addAttribute("name", name);
		
		System.out.println(
				"Data       name::::" + name + "::  acronym " + acronym + "::  paperid  ::  " + paperid);
		 
	}
	@RequestMapping("/list")
	public String listFiles(Model theModel) {
		System.out.println("Fetching  data from bucket ");
		List<FileInfo> files = new ArrayList<FileInfo>();
		;
		// get the files from the service
		files = storageController.getFile();
		System.out.println(StorageConfig.school);
		System.out.println("Files: " + files);
		// add the files to the model
		theModel.addAttribute("file", files);
		return "s3-bucket-files";

	}

	@RequestMapping("/delete")
	public String deleteFile(@RequestParam String fileName) {

		storageController.deleteFile(fileName);
		return "redirect:/contra/list";
	}
	private void sendMail(String file,String user,String email,String mesg) {
		 Mail mail = Mail.getmail();
		 String sub="File upload confirmation";
		 //String sub=" Aptaradropzone - The ZIP file exceeds 10 MB in size.So send it in this form";
		 StringBuffer sb = new StringBuffer();
			sb.append("Hi "+user+",");
			sb.append("<br>");
			sb.append("<br>");
			sb.append("You have successfully sent files to TAPS with following details:");
			sb.append("<br>");
			sb.append("<br>");
			sb.append("Name: "+user);
			sb.append("<br>");
			sb.append("Email Address: "+email);
			sb.append("<br>");
			sb.append("File Name: "+file);
			sb.append("<br>");
			sb.append("Message: "+mesg);	
			sb.append("<br>");
			sb.append("<br>");
			sb.append("Thanks,<br>");
			sb.append("ACM Production");
		 
			mail.sendMailHtmlFormat(MAIL_FROM,email, MAIL_CC, MAIL_BCC, sub,sb.toString());
			System.out.println("mail sent");
		
	}
	private void sendFailuremail(String file,String user,String email,String mesg) {
		 Mail mail = Mail.getmail();
		 String sub="File uploading failure notification";
		 //String sub=" Aptaradropzone - The ZIP file exceeds 10 MB in size.So send it in this form";
		 StringBuffer sb = new StringBuffer();
			sb.append("Hi "+user+",");
			sb.append("<br>");
			sb.append("<br>");
			sb.append("File uploading failed to TAPS with following details:");
			sb.append("<br>");
			sb.append("<br>");
			sb.append("Name: "+user);
			sb.append("<br>");
			sb.append("Email Address: "+email);
			sb.append("<br>");
			sb.append("File Name: "+file);
			sb.append("<br>");
			sb.append("Message: "+mesg);	
			sb.append("<br>");
			sb.append("<br>");
			sb.append("Thanks,<br>");
			sb.append("ACM Production");
		 
			mail.sendMailHtmlFormat(MAIL_FROM,"anil.tyagi@aptaracorp.com", "", MAIL_BCC, sub,sb.toString());
			System.out.println("Failure mail sent");
		
	}
	 
}
