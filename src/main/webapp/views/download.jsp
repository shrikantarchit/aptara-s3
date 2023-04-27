 
<%@page import="java.io.*"%>
 
<%@page import="java.io.IOException"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.BufferedInputStream"%>
<%@page import="java.io.ByteArrayOutputStream"%>
 
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 


<head>
 <script type="text/javascript" src="../js/jquery.js"></script>
 <script type="text/javascript" src="../js/FileSaver.js"></script>
</head>
<body > 
	  <button   id="save-btn">Start Proof</button><script>
$("#save-btn").click(function() {alert('click');
	  var blob = new Blob(["test text"], {type: "text/plain;charset=utf-8"});
	  //saveAs(blob, "testfile1.txt");
	  window.saveAs(blob, "testfile1.txt")
	  return blob;
	});
	</script>
	
<%
 
System.out.println("basepath  =" );
 
OutputStream os = null;
InputStream stream = null;
File f1 = new File("C:\\broker\\OUPJ");
if (!f1.exists()) {
       System.out.println("No Folder");
       f1.mkdir();
       System.out.println("Folder created");
   }
try{ System.out.println("inside try  =" );
	 
}catch(Exception e){
	e.printStackTrace();
}finally{
	//os.flush();
	 
}
%></body>
