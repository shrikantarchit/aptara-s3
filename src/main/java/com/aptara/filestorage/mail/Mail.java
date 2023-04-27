package com.aptara.filestorage.mail;
/*
 * Mail.java
 *
 * Created on November 9, 2006, 12:47 PM
 */

/**
 *
 * @author Atul
 */
import javax.mail.*;
import javax.mail.internet.*;
 import java.util.Properties;
import java.util.StringTokenizer;

public class Mail {
    
    /** Creates a new instance of Mail */
    private Mail() {
    }
    
    public static void setData(MailModel mailmodel){
    	MAIL_HOST = mailmodel.getMailhost();
    	DEFAULT_FROM = mailmodel.getMailfrom();
  //  	from = mailmodel.getFrom();
  //  	to = mailmodel.getTo();
 //   	cc = mailmodel.getCc();
    }
    
    public static Mail mail = null;
    
    public static Mail getmail(){
       	if(mail == null)mail = new Mail();
       	return mail;
    }
    
	//TEMPORARY
	public static String MAIL_HOST = "email-smtp.us-east-1.amazonaws.com";   //dedrelay.secureserver.net  earth.aptaracorp.com
	public static String DEFAULT_FROM = "";
	public static int isAWS=1;
    public String SMTP_USERNAME = "AKIA44MZ4HSU63GP53VC";
    public String SMTP_PASSWORD = "BKIr3DilEZJgjcXsnPYMcf+p9lq2XvqOvIsKh4Me21tk";
    
	public boolean sendMailHtmlFormat ( String MAIL_FROM, String MAIL_TO, String MAIL_CC, String MAIL_BCC, String subject, String text ){
		
		 
        text=text.replaceAll("�", "&ldquo;");
        text=text.replaceAll("�", "&rdquo;");
		if(isAWS==1){
			return awssendMailHtmlFormat( MAIL_FROM, MAIL_TO, MAIL_CC,MAIL_BCC,  subject, text);
		}else{
			return nonAws_sendMailHtmlFormat(MAIL_FROM,MAIL_TO,MAIL_CC,MAIL_BCC,subject,text );
		}
	}
	
   public boolean nonAws_sendMailHtmlFormat( String MAIL_FROM, String MAIL_TO, String MAIL_CC, String MAIL_BCC, String subject, String text )
   {
	   try
	   {
	   if (text== null || text.equals( "" ) )
		   return false;
	   if ( MAIL_TO== null || MAIL_TO.equals( "" ) )
		   return false;

		   Session mailSession;String addr = "";
		   Properties properties = System.getProperties();
		   //properties.put( "mail.smtp.host", GlobalBean.MAIL_HOST);
		   properties.put( "mail.smtp.host", MAIL_HOST);
		   properties.put("mail.smtp.connectiontimeout", 10000);
		   properties.put("mail.smtp.timeout", 10000);
		   mailSession = Session.getDefaultInstance( properties, null );
		   Message msg = new MimeMessage( mailSession );
		   ///set reply to////
		  // InternetAddress[] replyTo = getEmailIDss(MAIL_FROM);
		   //msg.setReplyTo(replyTo);
	   if (MAIL_FROM == null || MAIL_FROM.equals("")){
		   msg.setFrom(new InternetAddress ( DEFAULT_FROM ));
	   }
	   else{	
		   msg.setFrom(new InternetAddress ( MAIL_FROM ));
	   }
		   InternetAddress[] addressTo = getEmailIDs( MAIL_TO ) ;
			 msg.setRecipients( Message.RecipientType.TO, addressTo);
	   if ( MAIL_CC != null ){
		   if ( ! (MAIL_CC.equals( "" ))  ){
				   InternetAddress[] addressCc = getEmailIDs( MAIL_CC ) ;
				   msg.setRecipients( Message.RecipientType.CC, addressCc );
		   }
	   } 
	   if ( MAIL_BCC != null ){
		   if ( ! (MAIL_BCC.equals( "" ))  ){
				   InternetAddress[] addressBCc = getEmailIDs( MAIL_BCC ) ;
				   msg.setRecipients( Message.RecipientType.BCC, addressBCc );
		   }
	   } 
			
		   msg.setHeader("Mime-Version" , "1.0" );
		   msg.setSubject( subject );
		   msg.setSentDate( new java.util.Date() );
		   MimeBodyPart mbp1 = new MimeBodyPart();
		   mbp1.setText( text );
		   mbp1.setHeader( "Content-Type", "text/html" );
		   mbp1.setHeader( "Content-Transfer-Encoding", "base64" );
		   Multipart mp = new MimeMultipart();
		   mp.addBodyPart( mbp1 );
		   msg.setContent( mp );
		   long startTime = System.currentTimeMillis();
		   Transport.send( msg);
		   long endTime = System.currentTimeMillis();
		   System.out.println("time taken in milli second  "+(endTime-startTime));
		   return true;
	   }
	   catch ( MessagingException me )
	   {
		   System.out.println(" problem while sending mail  "+me.getMessage());
		   me.printStackTrace();
		   System.out.println(" frist time mail sending fail so, we are trying to send mail second time");
		   boolean b = nonAws_sendMailHtmlFormat_2ndtime(MAIL_FROM, MAIL_TO, MAIL_CC, MAIL_BCC, subject, text);
		   if(b){
			   System.out.println(" second time mail send successfully ");
		   }else{
			   System.out.println(" second time mail sending is also fail. ");
		   }
		   return b;
	   }
   }

   
   public boolean nonAws_sendMailHtmlFormat_2ndtime(String MAIL_FROM, String MAIL_TO, String MAIL_CC, String MAIL_BCC, String subject, String text )
   {
	 System.out.println("Sending mail secound time........... "); 
	 try
	   {
	   if (text== null || text.equals( "" ) )
		   return false;
	   if ( MAIL_TO== null || MAIL_TO.equals( "" ) )
		   return false;

		   Session mailSession;String addr = "";
		   Properties properties = System.getProperties();
		   //properties.put( "mail.smtp.host", GlobalBean.MAIL_HOST);
		   properties.put( "mail.smtp.host", MAIL_HOST);
		   properties.put("mail.smtp.connectiontimeout", 10000);
		   properties.put("mail.smtp.timeout", 10000);
		   mailSession = Session.getDefaultInstance( properties, null );
		   Message msg = new MimeMessage( mailSession );
	   if (MAIL_FROM == null || MAIL_FROM.equals("")){
		   msg.setFrom(new InternetAddress ( DEFAULT_FROM ));
	   }
	   else{	
		   msg.setFrom(new InternetAddress ( MAIL_FROM ));
	   }
		   InternetAddress[] addressTo = getEmailIDs( MAIL_TO ) ;
			 msg.setRecipients( Message.RecipientType.TO, addressTo);
	   if ( MAIL_CC != null ){
		   if ( ! (MAIL_CC.equals( "" ))  ){
				   InternetAddress[] addressCc = getEmailIDs( MAIL_CC ) ;
				   msg.setRecipients( Message.RecipientType.CC, addressCc );
		   }
	   } 
	   if ( MAIL_BCC != null ){
		   if ( ! (MAIL_BCC.equals( "" ))  ){
				   InternetAddress[] addressBCc = getEmailIDs( MAIL_BCC ) ;
				   msg.setRecipients( Message.RecipientType.BCC, addressBCc );
		   }
	   } 
			
		   msg.setHeader("Mime-Version" , "1.0" );
		   msg.setSubject( subject );
		   msg.setSentDate( new java.util.Date() );
		   MimeBodyPart mbp1 = new MimeBodyPart();
		   mbp1.setText( text );
		   mbp1.setHeader( "Content-Type", "text/html" );
		   mbp1.setHeader( "Content-Transfer-Encoding", "base64" );
		   Multipart mp = new MimeMultipart();
		   mp.addBodyPart( mbp1 );
		   msg.setContent( mp );
		   System.out.println(" Transport.send  .......... ");
		   long startTime = System.currentTimeMillis();
		   Transport.send( msg);
		   long endTime = System.currentTimeMillis();
		   System.out.println("time taken for secound time mail in milli second  "+(endTime-startTime));
		   return true;
	   }
	   catch ( MessagingException me )
	   {
		   System.out.println(" problem while second sending mail  "+me.getMessage());
		   me.printStackTrace();
		   return false;
	   }
   }
   
        public InternetAddress[] getEmailIDs ( String ids ) throws javax.mail.internet.AddressException
        {
        	if(ids==null){
        		InternetAddress addresses[] = new InternetAddress[0];
        		return addresses;
        	}
            int indx = 0;
            StringTokenizer st = new StringTokenizer( ids, ",;" );
            InternetAddress addresses[] = new InternetAddress[ st.countTokens() ];
            while ( st.hasMoreTokens() )
            {
                addresses[indx] = new InternetAddress( st.nextToken() );
                indx++;
            }
            return addresses;
        }
   //virtual mails
        
 
        public boolean awssendMailHtmlFormat(String MAIL_FROM, String MAIL_TO, String MAIL_CC, String MAIL_BCC, String subject, String text) {
        	// this part of code is for AWS for non sending mail to author/admin 
        	/*MAIL_FROM=mailfrom;
        	MAIL_TO = mailto;
        	MAIL_CC = mailcc;
        	MAIL_BCC = mailbcc;*/
        	String mailHost =MAIL_HOST;
        	boolean status = false;
              	
        //	String TO=MAIL_TO;
        	int PORT = 587;
       // 	String FROM = MAIL_FROM;
       // 	String FROMNAME = MAIL_FROM;
        	 
            // Create a Properties object to contain connection configuration information.
        	Properties props = System.getProperties();
        	props.put("mail.transport.protocol", "smtp");
        	props.put("mail.smtp.port", PORT); 
        	props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required","true");
        	
            props.put("mail.smtp.auth", "true");
          //  props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.smtp.ssl.trust", "*");


            //props.put("mail.debug", "true");
         
            // Create a Session object to represent a mail session with the specified properties. 
        	Session session = Session.getDefaultInstance(props);

            // Create a message with the specified information. 
            MimeMessage msg = new MimeMessage(session);
            Transport transport = null;
            try
            {
            msg.setFrom(new InternetAddress(MAIL_FROM,MAIL_FROM));
            InternetAddress[] addressTo = getEmailIDs( MAIL_TO ) ;
            msg.setRecipients(Message.RecipientType.TO, addressTo);
          //  msg.setRecipient(Message.RecipientType.TO, new InternetAddress(MAIL_TO));
            InternetAddress[] addressCC = getEmailIDs(MAIL_CC) ;
            msg.setRecipients(Message.RecipientType.CC, addressCC);
	       // msg.setRecipient(Message.RecipientType.CC, new InternetAddress(MAIL_CC));
            InternetAddress[] addressBCC = getEmailIDs(MAIL_BCC) ;
	        msg.setRecipients(Message.RecipientType.BCC, addressBCC);
	        //set reply to address
	        //InternetAddress[] replyTo = getEmailIDss(MAIL_FROM);
	     //   msg.setRecipient(Message.RecipientType.BCC, new InternetAddress(MAIL_BCC));
            msg.setSubject(subject);
            msg.setContent(text,"text/html");
            ////////changing reply to///
           // msg.setReplyTo(replyTo);
            
            /////end//////
            // Add a configuration set header. Comment or delete the next line if you are not using a configuration set
            //msg.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET);
            // Create a transport.
              transport = session.getTransport();
            // Send the message.
                 System.out.println("Sending...");
                transport.connect(mailHost, SMTP_USERNAME, SMTP_PASSWORD);
                // Send the email.
                transport.sendMessage(msg, msg.getAllRecipients());
                System.out.println("Email sent!");
                status = true;
            }
            catch (Exception ex) {
                System.out.println("The email was not sent.");
                System.out.println("Error message: " + ex.getMessage());
                ex.printStackTrace();
            }
            finally
            {
                // Close and terminate the connection.
                try {
    				transport.close();
    			} catch (MessagingException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            }
               return status;
    }
        
               
   public static void main(String[] args)
    {
	 //  MAIL_HOST = "email-smtp.us-east-1.amazonaws.com"; 
	   //new Mail().sendMailHtmlFormat( "dbadmin@aptaracorp.com", "archit.shrikant@aptaracorp.com", "", "", "hi", "hi");
    }
   //////////////reply to//////////
   public InternetAddress[] getEmailIDss ( String ids ) throws javax.mail.internet.AddressException
   {
	  // ids="architshrikant@gmail.com";
  
       int indx = 0;
       StringTokenizer st = new StringTokenizer( ids, ",;" );
       InternetAddress addresses[] = new InternetAddress[ st.countTokens() ];
       while ( st.hasMoreTokens() )
       {
           addresses[indx] = new InternetAddress( st.nextToken() );
           indx++;
       }
       return addresses;
   }
}