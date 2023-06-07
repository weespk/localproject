<!doctype html>
<html lang="en">
<head>
	<meta name="viewport" content="width=720">
	<meta charset="UTF-8">
	<meta name="Author" content="">
	<meta name="Keywords" content="">
	<meta name="Description" content="">
	<title>Document</title>
	<script src="//cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
	<style type="text/css">
		div {
			font-size: 25px;
		}
		
		.video {
			max-width: 720px;
			height: 400px;
			width: 100%;
			z-index: 100000;
		}
		
		.controll {
			position: absolute;
			right: 0px;
		}
		
		.list {
			position: relative;
			top: 35px;
			height: 600px;
			overflow: overlay;
		}
	</style>
</head>
<body style="margin: 0; padding: 0;">

	<div class='video'></div>

	<div class='controll'>
		<table>
			<tr>
				<td><button onclick="o.slow()">SLOW</button></td>
				<td><button onclick="o.fast()">FAST</button></td>
			</tr>
		</table>
	</div>

	<div class='list'></div>

	<script type="text/javascript">
		var o={
				change: function(src){
					var html='';
					html+='<video id="myVideo" width="100%" height="400" controls autoplay="autoplay">';
					html+='	<source src="/movie/fileName?fileName='+src+'" type="video/mp4"/>';
					html+='</video>	';
					$(".video").empty().append(html);
					
		// 			if(!window.myVod){
		// 				window.myVod= document.getElementById("myVideo");
		// 			}
					window.myVod= document.getElementById("myVideo");
				}
				, fast: function(){
					window.myVod.playbackRate = window.myVod.playbackRate + .25;
				}
				, slow: function(){
					window.myVod.playbackRate = window.myVod.playbackRate - .25;
				}
		};
		
		
		$(document).ready(function(){
			$.ajax({
				type: 'POST',
				url: '/movie/fileList',
				data: {  },
				dataType:"json",
				success: function(req) {
					var html= '';
					$(".list").append("<table>");
					$(req.list).each(function(id){
						$(".list").append('<tr><td ><a href="#'+this+'" onclick="o.change(\''+encodeURIComponent(this)+'\')">'+this+'</a></td></tr>');
					});
					$(".list").append("</table>");
				}
			});
		});
	</script>
</body>
</html>
