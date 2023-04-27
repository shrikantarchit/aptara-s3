<%@page import="java.io.*"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.BufferedInputStream"%>
<%@page import="java.io.ByteArrayOutputStream"%>
<!DOCTYPE html>
<html xmlns:th="http://wwww.thymeleaf.org">
<head>
<meta charset="ISO-8859-1"/>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>PowerManage</title>
    <!-- CSS file-->
    <link rel="shortcut icon" type="image/x-icon" sizes="" href="../images/1650626770.ico">
    
    <link href="../bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css" media="all">
    <link href="../css/applet.css" rel="stylesheet" type="text/css" media="all">
    <link href="../font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">
<link href="https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap" rel="stylesheet">
    
    <!-- JS file-->
    <script type="text/javascript" src="../js/jquery.js"></script>
    <script type="text/javascript" src="../bootstrap/js/bootstrap.min.js"></script>
    <!--[if lt IE 9]>
        <script src="js/html5shiv.js"></script>
        <script src="js/respond.min.js"></script>
    <![endif]-->
    
    <script>
    function start(){
    	 
    	//fetch('https://api.ipify.org/?format=json').then(results=>results.json()).then(data=>console.log('second '+data.ip));
    	
    	  
    	 $.ajax({
   			type:'GET',
   			url: 'http://192.168.140.96:8081/restcontra/downloadd/1657548755362_acpapp.zip',
   			data: "fileName="+file, 
   			async:false,
   			success: function(){
   				alert('ajax called');
   				}
      	 });
     }
    
   
    </script>
    
</head>

<body class="LgnCont"  >
	<header class="header sticky-top"><!--Start Header-->
		<div class="container">
			<div class="row">
				<div class="col-md-3 col-sm-5 log_brand">
					<a class="brand-logo"><img src="../images/logo.png" width="180"></a>
				</div>
				<div class="col-md-7 col-sm-3 nav_mid"><!--Start HeadMid Navbar-->
				</div><!--End HeadMid Navbar-->
				<div class="col-md-2 col-xs-4 col-sm-4 logo-right">
					<a class="brand-logo2" href="#"><img src="../images/logo-aptara.png" width="90"></a>
				</div>
			</div>
		</div>
    </header>
    <div class="linkBoxes container-fluid"><!--Start linkBOxes-->
		<div class="container">
			<div class="row">
				<div class="col-md-4 col-xs-4 colbx">
					<a href="">
						<!--<i class="fa fa-sitemap"></i>-->
						<p>User Name: <span class="itm">${username}  </span></p>  
					</a>              
				</div>
				<div class="col-md-4 col-xs-4 colbx">
					<a href="">
						<!--<i class="fa fa-group"></i>-->
						<p>Article Name: <span class="itm">${cref}</span></p>
					</a>
				</div>
				<div class="col-md-4 col-xs-4 colbx">
					<a href="">
						<!--<i class="fa fa-gear"></i>-->
						<p>Stage:  <span class="itm">${stage}</span></p>                
					</a>
				</div>
				<div class="col-md-4 col-xs-4 colbx">
					<a href="" target="_blank">
						<!--<i class="fa fa-line-chart"></i>-->
						<p>Task <span class="itm"> ${taskid}</span></p>
					</a>               
				</div>
				<div class="col-md-4 col-xs-4 colbx">
					<a href="">
						<!--<i class="fa fa-comments"></i>-->
						<p>Location: <span class="itm">Noida</span></p>
					</a>                
				</div>
			</div>
		</div>
	</div><!--End linkBOxes-->
	
	<div class="FormSection"><!--Start FormSection-->
		<div class="container">
			<h2><i class="fa fa-gears clblue"></i> Details</h2>
			<form method="get" action="/restcontra/download">
				<div class="row">
				  <div class="form-group col-xs-6">
					<label>Text Error</label>
					<input type="text" class="form-control" value="">
				  </div>
				  <div class="form-group col-xs-6">
					<label>Art error</label>                               
					<input type="text" class="form-control" value="">
				  </div>
				  <div class="form-group col-xs-6">
					<label>MMC Error</label>
					<input type="text" class="form-control" value="">
				  </div>
				  <div class="form-group col-xs-6">
					<label>MSP</label>                               
					<input type="text" class="form-control" value="">
				  </div>
				  <div class="form-group col-sm-12">
					<label>Remarks</label>                               
					<textarea class="form-control" rows="5"></textarea>
				  </div>
				</div>
				<div class="form-group col-xs-12 text-center">
			 
				  <button type="submit"   class="btn btn-primary">Start Proof</button>
			 
				  <button type="submit" class="btn btn-primary">Stop Proof</button>
				</div>
			</form>
		</div>
	</div>
	
    <footer><!--start Here Footer-->
    	<div class="container">
        	<div class="row">
                <div class="col-md-4 col-sm-6"><a href="#" class="foot-logo">Privacy Policy</a> | <a href="#" class="foot-logo">Contact Us</a></div>
                <div class="col-md-8 col-sm-6 text-right"><p>Copyright � 2022, aptaracorp.com</p></div>
            </div>
        </div>
    </footer><!--End Here Footer-->
</body>
</html>