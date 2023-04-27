<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script type="text/javascript" src="resources/js/jquery.1.4.2.js"></script>
<script type="text/javascript">
function hide()  {  
	   document.getElementById("progressBar").style.display="none";  
	 }
function _(el) {
  return document.getElementById(el);
}

function uploadFile() {  
	 document.getElementById("progressBar").style.display="block"; 
  var file = _("file1").files[0];
  // alert(file.name+" | "+file.size+" | "+file.type);
  var formdata = new FormData(); 
  formdata.append("file1", file);
  var ajax = new XMLHttpRequest();
  ajax.upload.addEventListener("progress", progressHandler, false);
  ajax.addEventListener("load", completeHandler, false); 
  ajax.addEventListener("error", errorHandler, false);
  ajax.addEventListener("abort", abortHandler, false);
  ajax.open("POST", "file_upload_parser.php"); 
  //use file_upload_parser.php from above url
  ajax.send(formdata); 
}

function progressHandler(event) {
  _("loaded_n_total").innerHTML = "Uploaded " + event.loaded + " bytes of " + event.total;
  var percent = (event.loaded / event.total) * 100;
  console.log(percent);
  if(percent=='100'){
	  
	  hide() ;
	  
  }
  _("progressBar").value = Math.round(percent);
  _("status").innerHTML = Math.round(percent) + "% uploaded... please wait";
}

function completeHandler(event) {
  //_("status").innerHTML = event.target.responseText;
  _("progressBar").value = 0; //wil clear progress bar after successful upload
}

function errorHandler(event) {
 // _("status").innerHTML = "Upload Failed";
}

function abortHandler(event) {
 // _("status").innerHTML = "Upload Aborted";
}
function fileValidation(fileName){
	if( document.getElementById("file1").value==""){
		 
		return false;
		
	}
	var fileInput =  document.getElementById("file1");
	var filesize=fileInput.files[0].size; 
  	var filetoupload ='${acronym}'+'-'+'${paperid}'+'.zip';
	var file=fileName.substring(fileName.lastIndexOf('\\')+1); 
 	if(file===filetoupload ){
 		  if(filesize > '1073741824'){
 			  alert('Upload file size should be less than 1GB.');
 			  return false;
 			  }
 		uploadFile();
 		return true;
 	}
 	else{
 		
 		alert('Upload file name should be '+'"'+filetoupload+'"');
 		return false;
 	}
}
function validation(){ 	 
	if(document.getElementById('name').value=="") {
        alert("enter something valid");
        return false;
    }	
}

</script>
<title>File Upload</title>
<meta name="viewport" content="width=device-width, initial-scale=1" />
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.1.0/css/bootstrap.min.css">
<script	src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script	src="https://maxcdn.bootstrapcdn.com/bootstrap/4.1.0/js/bootstrap.min.js"></script>
<style>
	header{padding:14px 0; border-bottom:8px solid #0093d0; margin-bottom:18px;}
	header h3{ margin-top:12px;}
	header h3 b{color:#0093d0;}
	.main-container{ border:1px solid #ddd; margin-top:15px;}
	.form-group{ max-width:400px;}
	input[required] {
  display: block;
}

input[required]::after {
  content: '*';
  color: red;
}
.textListRight{background-color: #eee; }
.textListRight ol{ margin: 100px 0 0; }
</style>
</head>

<body onload='hide()'>
<div class="container main-container">
	<header>
		<div class="col-md-12">
			<div class="row">
				<div class="col-sm-6">
					<img src="../images/logo2.png" alt="Association for Computing Machinery">
				</div>
				<div class="col-sm-6 text-right">
					<h3>Welcome <b>${name}</b></h3>
				</div>
				
				<hr style="height: 18px; border-width: 0; color: blue; background-color: blue">
			</div>
		</div>
	</header>
	
	<div *ngIf="fileupload.status==='error'">
		<div [innerHTML]="fileupload.message"></div>
	</div>
	<div class="row">
	<div class="col-sm-6" [hidden]="submitted">
		<form method="POST" enctype="multipart/form-data" id="UploadForm"
			onsubmit="return fileValidation(document.getElementById('file1').value);"
			action="/contra/authorFileupload">

			<div class="form-group">
				<label for="name">Name<span  style="color:red">*</span></label> 
				<input type="text" class="form-control"	name="name" id="name" required minlength="4">
			</div>

			<div class="form-group">
				<label for="name">Email Address<span  style="color:red">*</span></label> <input type="email"	class="form-control" required name="emailId" oninvalid="this.setCustomValidity('Please add a valid email address.')" oninput="this.setCustomValidity('')">
			</div>

			<div class="form-group">
				<label for="name">Subject</label> 
				<input type="text" class="form-control" name="subject">
			</div>
			<div class="form-group">
				<label for="name"></label>
				<textarea rows="3" class="form-control" cols="52" placeholder="Message" id="message" name="message"></textarea>
			</div>

			<div class="msgrow">
				<div class="row">
					<div class="form-group col-sm-6">
						<label class="control-label" for="upload">Upload Your File:</label>
						<input id="file1" type="file" class="multipart/form-control"
							onchange="" placeholder="Upload File" name="file"> <span
							style="color: red; white-space: nowrap;">Note: System will accept<b>
								${acronym}-${paperid}.zip</b> only.  
						</span>
					</div>
					<div class="form-group col-sm-12">
						<progress id="progressBar" value="0" max="100" style="width: 100%;"></progress>
						<h3 id="status"></h3>
						<p id="loaded_n_total"></p>
					</div>
				</div>
			</div>
			
			<div>
				<button type="submit" class="btn btn-primary" id="btnSubmit">Upload</button>
			</div>
		</form>
	</div>
	<div class="col-sm-5 textListRight">
		<ol>
			<li>This is a FTP upload service from TAPS for zip files greater than 10 MB in size.</li>
			<li>Please do not add supplementary files in this upload as there is a separate link for providing those; please check your dashboard for that link.</li> 
			<li>Please wait till you get a “success” message for uploading your file, before closing this window.</li>
		</ol>
	</div>
	</div>


	<div style="color: red;">${msg}</div>
</div>
</body>
</html>
