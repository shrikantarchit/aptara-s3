package com.aptara.filestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class DemoApplication  extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		System.out.println("dev change");
	}

	 @Override
	  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		 setRegisterErrorPageFilter(false);
	    return builder.sources(DemoApplication.class);
	    
	  }
 
}
