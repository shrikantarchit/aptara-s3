package com.aptara.filestorage.config;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

@Configuration
public class StorageConfig {
    static public String school=new String("dav");
    String s="abcd";
    String p="abcd";
    
	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;
	@Value("${cloud.aws.credentials.secret-key}")
	private String accessSecret;
	@Value("${cloud.aws.region.static}")
	private String region;

	@Bean
	public AmazonS3 s3Client() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, accessSecret);
		return AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(region).build();
		 
	}
	
	@Bean
	public TransferManager transferManager(){
		
		TransferManager tm = TransferManagerBuilder.standard()
									.withS3Client(s3Client())
									.withDisableParallelDownloads(false)
									.withMinimumUploadPartSize((long) (5 * 1024 * 1024)) 
									.withMultipartUploadThreshold((long) (16 * 1024 * 1024))
									.withMultipartCopyPartSize((long) (5 * 1024 * 1024))
									.withMultipartCopyThreshold((long) (100 * 1024 * 1024))
									.withExecutorFactory(()->createExecutorService(20))
									.build();
		
		int oneDay = 1000 * 60 * 60 * 24;
		Date oneDayAgo = new Date(System.currentTimeMillis() - oneDay);
		
		try {
			
			tm.abortMultipartUploads("archit-aptara", oneDayAgo);
			
		} catch (AmazonClientException e) {
			System.out.println("Unable to upload file, upload was aborted, reason: " + e.getMessage());
		}
		
		return tm;
	}
	
	private ThreadPoolExecutor createExecutorService(int threadNumber) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int threadCount = 1;

            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("jsa-amazon-s3-transfer-manager-worker-" + threadCount++);
                return thread;
            }
        };
        return (ThreadPoolExecutor)Executors.newFixedThreadPool(threadNumber, threadFactory);
    }
	  @SuppressWarnings("deprecation")
	@Bean
	    public WebMvcConfigurer corsConfigurer() {
	        return new WebMvcConfigurerAdapter() {
	            @Override
	            public void addCorsMappings(CorsRegistry registry) {
	                registry.addMapping("/**")
	                        .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH");
	            }
	        };
	    }
	  public void get() {
		  System.out.println(school);
	  }
	
}
