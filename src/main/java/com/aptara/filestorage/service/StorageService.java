package com.aptara.filestorage.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
 

import lombok.extern.slf4j.Slf4j;

@Primary
@Service
@Slf4j
public class StorageService {

	private final static Logger log = Logger.getLogger(StorageService.class.getName());
    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;
    
	 
	
    public StorageService() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String uploadFile(MultipartFile file) {
        File fileObj = convertMultiPartFileToFile(file);
		if ( fileObj.toString() == null && fileObj.toString().isEmpty()) {
			return  null;
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
        fileObj.delete();
        return "File uploaded : " + fileName;
    }


    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
public List<S3ObjectSummary> getfiles() {
		
		ObjectListing listing = s3Client.listObjects( bucketName);
		List<S3ObjectSummary> summaries = listing.getObjectSummaries();

		while (listing.isTruncated()) {
		   listing = s3Client.listNextBatchOfObjects (listing);
		     summaries.addAll(listing.getObjectSummaries());
		}
		System.out.println(summaries); 
		return summaries;
		
	}


    public String deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
        return fileName + " removed ...";
    }


    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
        	log.info("Error converting multipartFile to file"+ e);
          
        }
        return convertedFile;
    }
}
