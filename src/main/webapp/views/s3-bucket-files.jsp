<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="java.util.List"%>

<html>
<script>

</script>
<head>
<title>List Files</title>


</head>
<body>

	<div id="wrapper">
		<div id="header">
			<h2>S3-bucket-files</h2>
		</div>
	</div>
	<div id="container">
		<div id="content">


			<!--  Add Button -->
			<input type="button" value="Add Files"
				onclick="window.location.href='homepage';return false;"
				class="add-button" />





			<!--  add out table here -->
			<table border="1">

				<tr ><th>File Name</th>
			<!-- 		<th>File Etag ID</th> -->
					<th>File Size</th>
					<th>Action</th>
				</tr>

				<c:forEach var="eList" items="${file}">

    <c:url var="downloadLink" value="/restcontra/downloadd">
   <c:param name="fileName" value="${eList.fileName }"/>
   </c:url> 
   
    <c:url var="deleteLink" value="/contra/delete">
   <c:param name="fileName" value="${eList.fileName }"/>
   </c:url> 
					<tr>
						<td>${eList.fileName}</td>
				<!-- 		<td>${eList.sno}</td> -->
						<td>${eList.size } bytes</td>
<td>
   
     <!-- download link  -->
   <a href="${downloadLink}">Download</a>
   |
   <a href="${deleteLink}"
   onclick="if (!(confirm('Are you sure you want to delete this File?'))) 
	   return false">Delete</a>
   
    <!-- delete link  -->
    
    
   </td>
					</tr>

				</c:forEach>
			</table>




		</div>
	</div>
</body>
</html>