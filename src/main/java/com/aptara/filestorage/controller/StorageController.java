package com.aptara.filestorage.controller;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.IOUtils;
import com.aptara.filestorage.dao.FileInfo;
import com.aptara.filestorage.ftp.ApacheSFtp;
import com.aptara.filestorage.mail.Mail;
import com.aptara.filestorage.service.StorageService; 

@RestController
@RequestMapping("/restcontra")
//@CrossOrigin(origins = "*")
public class StorageController {
	private static String MAIL_FROM = "powermanage@aptaracorp.com";
	private static String MAIL_TO = "archit.shrikant@aptaracorp.com";// #niraj
																		// "lokesh.paliwal@aptaracorp.com";
	private static String MAIL_CC = "";
	
	Logger logger = LoggerFactory.getLogger(StorageController.class);
	@Autowired
	private StorageService service;

	// New Upload with user details

	@PostMapping("/upload-details")
	public String createUserDetails(@RequestParam String data, @RequestParam(value = "file") MultipartFile file) {
		// return employeeRepository.save(employee);
		System.out.println(file.getOriginalFilename());
		return data;
	}

	 

	@GetMapping(value = "/test")
	public String getFiless() {

		return "download";
	}

	@PostMapping("/upload")
	public String uploadFile(@RequestParam(value = "file") MultipartFile file) {

		System.out.println("File uploading in progress..");
		try {
			TimeUnit.SECONDS.sleep(10);
			System.out.println("Thread live");
			return "File uploaded";
		} catch (Exception e) {
			return null;
		}

	}

	@Value("${application.bucket.name}")
	private String bucketName;

	// test//
	@Autowired
	protected TransferManager transferManager;

	/**
	 * UPLOAD FILE to Amazon S3
	 */
	@PostMapping(value = "/uplod", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> uploadFile(@ModelAttribute("user") String user,
			@RequestParam(value = "file", required = true) MultipartFile file) {
		  
		HttpHeaders resHeader = new HttpHeaders();
		boolean uplflg= false;
		boolean renameflg= false;
		 Mail mail = Mail.getmail();
		 String name="";
		 String email="";
		try {
			
			JSONObject object = new JSONObject(user); 
			name=object.getString("name");
			email=object.getString("emailId");
			System.out.println("name >>>>>> "+object.getString("name"));  
			  
			// FTP server upload starts
			InputStream ins = file.getInputStream();
			ApacheSFtp sourceFtp = null;
			
			sourceFtp = new ApacheSFtp("apollo.aptaracorp.com", "brokerapo", "u8vWkj9a", "1722");
			uplflg=sourceFtp.UploadFile("/apol4/PowerManage/dev/Archit_test", file.getOriginalFilename()+"_FTPSynk", ins, "");
			ins = null;
			sourceFtp = null;
			// FTP server upload ends
			
			System.out.println("File upload method called " + user);
			if(uplflg) {
				sourceFtp = new ApacheSFtp("apollo.aptaracorp.com", "brokerapo", "u8vWkj9a", "1722");
				renameflg=sourceFtp.RenameFile("/apol4/PowerManage/dev/Archit_test", file.getOriginalFilename()+"_FTPSynk", file.getOriginalFilename(), "");
				Thread.sleep(1000);
				if(renameflg) {
				   // email to workgroup	
				}
			}
			
			// S3 bucket upload starts
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(file.getSize());
			metadata.setContentType(file.getContentType());
			// final PutObjectRequest request = new PutObjectRequest(bucketName, "Demo.zip", new File("D:\\archit\\Demo.zip"));
			final PutObjectRequest request = new PutObjectRequest(bucketName, file.getOriginalFilename(),
					file.getInputStream(), metadata);
			request.setGeneralProgressListener(new ProgressListener() {
				@Override
				public void progressChanged(ProgressEvent progressEvent) {
					String transferredBytes = "Uploaded bytes: " + progressEvent.getBytesTransferred();
					//logger.info(transferredBytes);
				}
			});
			System.out.println(file.getOriginalFilename() + " uploading start for " + user + new Date());
			 Upload upload = transferManager.upload(request);
			 
			// Or you can block and wait for the upload to finish

			 upload.waitForCompletion();
			 sendMail(file.getOriginalFilename(),name,email);
			// S3 bucket upload ends
			System.out.println(file.getOriginalFilename() + " uploaded for " + user + new Date());

			resHeader.set("origin", "http://localhost:8080");
			resHeader.set("Content-Type", "application/json");
			resHeader.set("Accept", "application/json");
			 return ResponseEntity.ok().headers(resHeader).body("success");
			//return ResponseEntity.ok("User registered successfully!");
			//send mail
			
			 
		} catch (Exception e) { 	
			System.out.println(e.getMessage());
			resHeader.set("origin", "http://localhost:8080");
			resHeader.set("Content-Type", "application/json");
			resHeader.set("Accept", "application/json"); 
			 return ResponseEntity.badRequest().headers(resHeader).body("failure");
			//return ResponseEntity.ok("User registered successfully!");
		}
		

	}
 

	private void sendMail(String file,String user,String email) {
		 Mail mail = Mail.getmail();
		 mail.sendMailHtmlFormat("taps@aptaracorp.com",MAIL_TO, "", "", file,"Name :"+user+" "+"Email :"+email);
		
	}



	@GetMapping("/downloadd/{fileName}")
	public ResponseEntity<Object> downloadFilee(@PathVariable String fileName) {
		System.out.println("response entity file download");
		byte[] data = service.downloadFile(fileName);
		ByteArrayResource resource = new ByteArrayResource(data);

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers)
					.contentLength(fileName.length()).contentType(MediaType.parseMediaType("application/txt"))
					.body(resource);

			return responseEntity;

			// return ResponseEntity.ok().contentLength(data.length).header("Content-type",
			// "application/octet-stream")
			// .header("Content-disposition", "attachment; filename=\"" + fileName +
			// "\"").body(resource);
		} catch (Exception e) {
			System.out.println("No such file exist " + e.getMessage());
			ResponseEntity<ByteArrayResource> resources = null;
			return null;
		}
	}
	@GetMapping("/downloadd")
	public HttpServletResponse downloadFileee(@RequestParam String fileName,HttpServletResponse response) {
		//byte[] data = service.downloadFile(fileName);
		//ByteArrayResource resource = new ByteArrayResource(data);
		System.out.println("downloadFileee starts");
		ByteArrayResource resource = null;
		
		ApacheSFtp sourceFtp = null;
		InputStream is = null;
		String serverpath = "/apol4/PowerManage/dev/Archit_test/ELSST079-18";
		String zipFile = "";
		ByteArrayResource resources=null;
		byte[] content =null;
		try {
			String TARGETt = "C:\\broker\\OUPJ\\BBBIOC\\BBBIOC_00_00\\zbaa017\\"+zipFile;
			File file = new File(TARGETt);
			sourceFtp = new ApacheSFtp("apollo.aptaracorp.com", "brokerapo", "u8vWkj9a", "1722");
			List<String> ftpfiles = new ArrayList<String>();
			ftpfiles = sourceFtp.listAll(serverpath, "");
			sourceFtp = null;
			for (String filee : ftpfiles) {
				if (filee.endsWith(".zip")) {
					zipFile = filee;
				}
			}
			sourceFtp = new ApacheSFtp("apollo.aptaracorp.com", "brokerapo", "u8vWkj9a", "1722");
			is = sourceFtp.getInputStream(serverpath, zipFile, "");
			sourceFtp = null;

			content = IOUtils.toByteArray(is);
			 
			//resource = new ByteArrayResource(content);
			 
	         
	        // get MIME type of the file
	        String mimeType = null;
	        if (mimeType == null) {
	            // set to binary type if MIME mapping not found
	            mimeType = "application/octet-stream";
	        }
	        System.out.println("MIME type: " + mimeType);
	 
	        // set content attributes for the response
	        response.setContentType("application/download");
	        response.setContentLength(content.length);
	 
	        // set headers for the response
	        String headerKey = "Content-Disposition";
	        String headerValue = String.format("inline; filename=\"%s\"",
	        		zipFile);
	        response.setHeader(headerKey, headerValue);
	 
	        // get output stream of the response
	        OutputStream outStream = response.getOutputStream();
	        outStream.write(content);
	        //byte[] buffer = new byte[1024];
	        int bytesRead = -1;
	 
	        // write bytes read from the input stream into the output stream
			 
			  //  while ((bytesRead = is.read(content)) != -1) { outStream.write(content, 0, bytesRead);  }
			 
	        //response.getWriter().print(outStream);
			    is.close();
	        outStream.close();
			/*
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers)
					.contentLength(fileName.length()).contentType(MediaType.parseMediaType("application/txt"))
					.body(resource);
*/
			return null;

			// return ResponseEntity.ok().contentLength(data.length).header("Content-type",
			// "application/octet-stream")
			// .header("Content-disposition", "attachment; filename=\"" + fileName +
			// "\"").body(resource);
		} catch (Exception e) {
			System.out.println("No such file exist " + e.getMessage());
			ResponseEntity<ByteArrayResource> resourcess = null;
			return response;
		}
	}


	@GetMapping("/download")
	public ResponseEntity<Object> downloadFile() throws Exception {
		ResponseEntity<Object> response=null;
		 System.out.println("Downloading started  ");
		  String fileName="ELSST079-18.zip";
		  String TARGET = "C:\\broker\\OUPJ\\BBBIOC\\BBBIOC_00_00\\zbaa017\\"+fileName;
		// System.out.println(TARGET);
	//	 Socket sock = new Socket(ip,8089);
		// System.out.println("sock obj >> "+sock);
		// String source = "//"+ip+"/C:/broker/OUPJ/BBBIOC/1631112659.jpg";
		 
		 
		// ftp article download start
	 	ApacheSFtp sourceFtp = null;
		InputStream is = null;
		String serverpath = "/apol4/PowerManage/dev/Archit_test/ELSST079-18";
		String zipFile = "";
		ByteArrayResource resource=null;
		byte[] content =null;
		try {
			sourceFtp = new ApacheSFtp("apollo.aptaracorp.com", "brokerapo", "u8vWkj9a", "1722");
			List<String> ftpfiles = new ArrayList<String>();
			ftpfiles = sourceFtp.listAll(serverpath, "");
			sourceFtp = null;
			for (String file : ftpfiles) {
				if (file.endsWith(".zip")) {
					zipFile = file;
				}
			}
			fileName=zipFile;
			sourceFtp = new ApacheSFtp("apollo.aptaracorp.com", "brokerapo", "u8vWkj9a", "1722");
			is = sourceFtp.getInputStream(serverpath, zipFile, "");
			sourceFtp = null;

			content = IOUtils.toByteArray(is);
			
			//is = null;
			resource = new ByteArrayResource(content);
			String TARGETt = "C:\\broker\\OUPJ\\BBBIOC\\BBBIOC_00_00\\zbaa017\\"+zipFile;
			/*
			 * File file = new File(TARGETt); OutputStream out = new FileOutputStream(file);
			 * 
			 * out.write(content); // write out the file we want to save. out.close();
			 */
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			
			//anil sir code
			// response.ok().contentLength(content.length).header("Content-type", "application/octet-stream") .header("Content-disposition", "attachment; filename=\"" + zipFile + "\"").body(resource);
			 ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers)
						.contentLength(fileName.length()).contentType(MediaType.parseMediaType("application/txt"))
						.body(resource);
			  System.out.println(" out.close() "+responseEntity);
			 return responseEntity;
			 
			 //ends
			 //return ResponseEntity.ok().contentLength(content.length).header("Content-type", "application/octet-stream") .header("Content-disposition", "attachment; filename=\"" + zipFile + "\"").body(resource);
		} 
		catch (Exception e1) { 
			e1.printStackTrace();
			System.out.println(e1.getMessage());
			ResponseEntity<ByteArrayResource> resources = null;
			System.out.println("response::::"+response);
			
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			
			//anil sir code
			// response.ok().contentLength(content.length).header("Content-type", "application/octet-stream") .header("Content-disposition", "attachment; filename=\"" + zipFile + "\"").body(resource);
			 ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers)
						.contentLength(fileName.length()).contentType(MediaType.parseMediaType("application/txt"))
						.body(resource);
			  System.out.println(" out.close() "+responseEntity);
			 return responseEntity;
			
			 
			 //return ResponseEntity.ok().contentLength(content.length).header("Content-type", "application/octet-stream") .header("Content-disposition", "attachment; filename=\"" + zipFile + "\"").body(resource);
		}  finally {
			if(is!=null) {
				is=null;
			}
		}
		// ftp article download ends

		/*// s3 bucket file download start 
		 		  try {
			  
			  File file = new File(TARGET);
			  OutputStream out = new FileOutputStream(file);
			  // Write your data
			  out.close();
			  
			   byte[] data = service.downloadFile(fileName);
		 System.out.println("data::"+data);
		 ByteArrayResource resourcee = new ByteArrayResource(data);
		 System.out.println("resource::"+resourcee);
        // close the data input stream
       FileOutputStream fos = new FileOutputStream(new File(TARGET)); //FILE Save Location goes here
       System.out.println("FileOutputStream"+fos);
       fos.write(data);  // write out the file we want to save.
       fos.close();
			  
		 byte[] data = service.downloadFile(fileName);
		 System.out.println("data::"+data);
		 ByteArrayResource resource = new ByteArrayResource(data);
		 System.out.println("resource::"+resource);
          // close the data input stream
         FileOutputStream fos = new FileOutputStream(new File(TARGET)); //FILE Save Location goes here
         System.out.println("FileOutputStream"+fos);
         fos.write(data);  // write out the file we want to save.
         fos.close();
         
		  return
		  ResponseEntity.ok().contentLength(data.length).header("Content-type",
		  "application/octet-stream") .header("Content-disposition",
		  "attachment; filename=\"" + fileName + "\"").body(resource); } 
		  catch (Exception e) { 
			  System.out.println("No such file exist " + e.getMessage());
		  ResponseEntity<ByteArrayResource> resources = null; return resources;
		  }
		 
		// s3 bucket file download end */
	}

	@RequestMapping("/delete")
	public void deleteFile(String fileName) {
		System.out.println("Deleting started ");
		new ResponseEntity<>(service.deleteFile(fileName), HttpStatus.OK);
		System.out.println(fileName + " Deleted ");
	}

	private List<FileInfo> infos = new ArrayList<FileInfo>();
	 
	@RequestMapping("/getfile")
	public List<FileInfo> getFile() {

		System.out.println("Getting files from S3 bucket");

		List<S3ObjectSummary> objectSummary = service.getfiles();
		int bucketSize = objectSummary.size();
		System.out.println("S3 bucket size=" + bucketSize);
		FileInfo info = new FileInfo();
		int j = 1;
		infos.removeAll(infos);
		for (int i = 0; i < objectSummary.size(); i++) {

			String FileName = objectSummary.get(i).getKey();
			long Size = objectSummary.get(i).getSize();
			String sno = objectSummary.get(i).getETag();
			infos.add(new FileInfo(sno, FileName, Size));
			System.out.println("Info: " + infos);
		}

		/*
		 * for (int i = 0; i < objectSummary.size(); i++)
		 * 
		 * {
		 * 
		 * String s = objectSummary.get(i).getKey(); System.out.println("Element No " +
		 * j + " : " + s); int k = j++;
		 * 
		 * String FileName = objectSummary.get(i).getKey(); long Size =
		 * objectSummary.get(i).getSize(); info.setSno("1"); info.setFileName(FileName);
		 * info.setSize(Size);
		 * 
		 * 
		 * }
		 */

		System.out.println("File Details: " + infos);
		return infos;
	}
 
}
