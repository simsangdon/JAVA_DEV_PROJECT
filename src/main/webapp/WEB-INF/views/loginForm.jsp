<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<!-- servlet-context.xml 파일의 <resources mapping="/resources/**" location="/WEB-INF/resources/" /> 설정으로 경로 변경 -->
<script src='/erp/resources/jquery-1.11.0.min.js' type='text/javascript'></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>로그인</title>
</head>
<body>
	<center>
		<form name="loginForm" class="loginForm" method="post" action="/erp/loginProc.do">
			<b>[관리자 로그인]</b>
			<table>
				<tr>
					<td heigh=2></td>
				</tr>
			</table>
			<table border=1 cellpadding=5 cellspacing=0 bordercolor='gray' class='tacss1'>
				<tr>
					<th bgcolor='#E1E1E1' align='center'>
						아이디
						<td>
							<input type='text' name='admin_id' class='admin_id' size='20'/>
						</td>
					</th>
				</tr>
				<tr>
					<th bgcolor='#E1E1E1' align='center'>
						암호
						<td>
							<input type='password' name='pwd' class='pwd'/>
						</td>
					</th>
				</tr>
			</table>
			<table>
				<tr>
					<td heigh=2></td>
				</tr>
			</table>
			<input type='button' value='로그인' name='login' class='login'/>
			<table>
				<tr>
					<td heigh=2></td>
				</tr>
			</table>
		</form>
	<div class="xxx"></div>
	</center>
</body>
</html>

<script>
	/**
	 * body 태그 안에 소스를 모두 실행한 후에 실행할 자바스트립트 코드 설정
	 */
	$(document).ready(function() {
		// class=login 을 가진 태크를 클릭하면 checkLoginForm() 함수를 호출
		// 하나의 body안에 form이 여러개일수 있고 form안에 class가 같은것들이 있을수 있기 때문에 form먼저 selector로 선택 하고 .class로 하는것이 좋다.
		// $("[name=loginForm]").find(" .login").click(function() {
		// $(".loginForm .login").click(function() {
		// $(".loginForm").find(".login").click(function() {
		// $("[name=loginForm] [name=login]").click(function() {
		$("[name=loginForm] .login").click(function() {
			checkLoginForm();
		});
	});
	/**
	 * 로그인 정보 유효성 체크
	 */
	function checkLoginForm() {
		/* 웹브라우저에서 입력한 [관리자 아이디]를 얻어 변수에 저장 */
		//var admin_id = document.loginForm.admin_id.value; // <=== script 스타일
		var admin_id = $('.admin_id').val(); // <=== jQuery 스타일
		/* 웹브라우저에서 입력한 [관리자 암호]를 얻어 변수에 저장 */
		var pwd      = $('.pwd').val();      // <=== jQuery 스타일
		if(admin_id == '') {
			alert('관리자 아이디 입력 요망');
			$('.admin_id').focus();
			return false;  // return 함수를 중지 하고 return의 오른쪽 데이터를 함수 호출 부분으로 전달
		}
		
		if(pwd == '') {
			alert('관리자 암호 입력 요망');
			$('.pwd').focus();
			return false;
		}
		
		/**
		 * 현재 화면에서 페이지 이동 없이(=비동기 방식으로) 서버쪽
		 * "/erp/loginProc.do" 화면을 호출하여
		 * [관리자 로그인 아이디의 존재 개수 문자열]을 응답 밥아 존재 개수가 1이면
		 * [연락처 검색 화면]으로 이동.
		 */
		$.ajax({
			/**
			 * 서버쪽 호출 URL 주소 지정
			 */
// 			url     : '/erp/loginProc.do'
			url     : '/erp/loginProcTest.do'
			/**
			 * form 태그 안의 데이터를 보내는 방법 지정
			 */
		  , type    : 'post'   
			/**
			 * 서버에 보낼 파라미터명과 파라미터값을 설정
			 */
		  , data    : {'admin_id':admin_id, 'pwd':pwd}
		    /**
		     * 서버의 응답을 성공적으로 받았을 경우 실행할 익명함수 설정/
		     * 익명함수의 매개변수 data 에는 [로그인 아이디의 존재 개수 문자열]이 들어 온다.
		     */
		  , success : function(data) {  // data 결과 값 -1이면 실패
// 			   loginProc.do 호출시
// 			   if(data == '1') {
// 				   // location.replace('/erp/contactSearchForm1.do');
// 				   console.log('에러없이 바로 열리면 하늘이 내린 저주인대...');
// 			   } else if(data == '0') {
// 				   alert('로그인 아이디가 아닙니다 재입력 요망!');
// 			   } else {
// 				   alert('서버쪽에서 에러발생!');				   
// 			   }		  	   

// 			   loginProcTest.do 호출시
// 		  	   $(".xxx").append(data);
// 		  	   $(".xxx").html(data);
		  	   alert( data );
		  	   var adminCnt = $.trim($(data).text());  // $(data).text();는 controller의 ModelAndView 안의 값이 추출된다.
		  	   console.log(adminCnt);
		  	   alert(adminCnt.length);
			   if(adminCnt == '1') {
				   // location.replace('/erp/contactSearchForm1.do');
				   console.log('에러없이 바로 열리면 하늘이 내린 저주인대...');
			   } else if(adminCnt == '0') {
				   alert('로그인 아이디가 아닙니다 재입력 요망!');
			   } else {
				   alert('서버쪽에서 에러발생!');				   
			   }		  	   
		    }
		  , error   : function() {
			   alert('서버 접속 실패!');  
			}
		});
	}
</script>