<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
 
<script>
function hide()  {  
	   document.getElementById("progressBar").style.visibility="hidden";  
	   
	}
function _(el) {
  return document.getElementById(el);
}

function uploadFile() {
	 document.getElementById("progressBar").style.visibility="visible"; 
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
  _("progressBar").value = Math.round(percent);
  _("status").innerHTML = Math.round(percent) + "% uploaded... please wait";
}

function completeHandler(event) {
  _("status").innerHTML = event.target.responseText;
  _("progressBar").value = 0; //wil clear progress bar after successful upload
}

function errorHandler(event) {
  _("status").innerHTML = "Upload Failed";
}

function abortHandler(event) {
  _("status").innerHTML = "Upload Aborted";
}
</script>


    <title>File Upload</title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.1.0/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.1.0/js/bootstrap.min.js"></script>
</head>

<body onload='hide()'>
 
 <script>
 
</script>
<div>Welcome ---->> ${username}</div>
<div>Job ID  ---->> ${jobid}</div>
 <div>item id ---->>  ${itemid}</div>
 <div>usr id  ---->> ${usr}</div>
 <div>client reference ---->>   ${cref}</div>
 <div>stage id ---->> ${stage}</div>
 <div>task id  ---->> ${taskid}</div>
 <div>journal  ---->> ${journal}</div>
 
 
  <div style="color:red;">${msg}</div>
 

<div class="container h-100">
    <div class="h-100">
        <div class="row h-100 justify-content-center align-items-center">
            <div class="col-sm-5">
                <h2><a href="/filestorage/contra/list">Click here to see files</a></h2>
                <form method="POST"  enctype="multipart/form-data" id="UploadForm" onsubmit="  uploadFile()" action="/contra/fileupload"  >
                    <div class="form-group">
                        <label class="control-label" for="upload">Upload Your File:</label>
                        <input id="file1" type="file" class="multipart/form-control" id="upload" placeholder="Upload File"  name="file">
                    </div>
                  <progress id="progressBar" value="0" max="100" style="width:300px;"></progress>
  <h3 id="status"></h3>
  <p id="loaded_n_total"></p>
    <button type="submit" class="btn btn-default" id="btnSubmit" >Upload</button>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
</html>
