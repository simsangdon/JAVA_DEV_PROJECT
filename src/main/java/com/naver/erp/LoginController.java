package com.naver.erp;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 가상 URL 주소로 접속하면 호출되는 메소드를 소유한 [LoginController 컨트롤러 클레스] 선언
 * @Controller 를 붙임으로서 [컨트롤러 클레스]임을 지정한다.
 * @Controller, @Service, @Repository 가 붙어 있는 것들은 자동으로 bean테그로 등록해 준다(servlet-context.xml의 <context:component-scan base-package="com.naver.erp" />)
 */
@Controller // <=== @Controller 어노테이션을 붙여줌으로서 실제로 Spring의 Controller의 역할을 할수 있도록 Spring에서 설정해 준다.   
public class LoginController {
	/**
	 * @Autowired 는 인터페이스를 상속 받아 구현한 객체를 셋팅 한다.
	 * LoginService 인테페이스를 상속 받은 LoginServiceImpl 객체를 loginService이 객체 변수에 할당한다.
	 * 이것의 장점은 인테페이스를 상속 받아 구현한 LoginServiceImpl의 명의 변경되어도 소스 수정이 없다는 것이다.
	 * 속성변수 loginService를 선언하고, LoginService 라는 인터페이스를 구현한
	 * [LoginServiceImpl 객체]를 생성해 저장
	 * @Autowired 가 붙은 속성변수에는 인터페이스 자료형을 쓰고
	 * 이 인터페이스를 구현한 클래스를 객체화하여 저장한다.
	 * LoginService 라는 인터페이스를 구현한 클래스의 이름이 몰라도 관계없다.
	 * 1개 존재하면 된다.(1개이상이면 에러가 난다.)
	 */
	@Autowired
	/* private LoginServiceImpl loginservice = new LoginServiceImpl(); */
	private LoginService loginService;
	
	@RequestMapping(value="loginForm.do")  // <=== loginForm 함수 호출시 실제로 호출할 가상 주소 설정
	public String loginForm() {
		/**
		 * WEB-INF/views/loginForm.jsp";  처림 실제 jsp경로를 적어준다.
		 * /servlet-context.xml 파일의 
		 * <beans:bean class="org.springframework.web.servlet.view.InternalResourceViewResolver"> 
		 * 에서 jsp파일의 경로를 설정한다.
		 * 
		 * [컨트롤러 클래스]의 메소드에 @ResponseBody가 없고, @RequestMapping가 붙어 있으면
		 * 메소드의 리턴형이 String 일 경우 리턴하는 문자열은 호출할 jsp페이지명 이다.
		 * 
		 * @ResponseBody가 붙어 있으면 return의 String값은 jsp명이 아닌 return 값으로 인식 한다. 
		 */
		return "loginForm"; 
	}
	
	/**
	 * DB리턴값이 있는 경우에는 
	 * ModelAndView 이거를 사용 해야 한다.
	 * loginForm함수와 같은 함수
	 */
	@RequestMapping(value="loginFormTest.do")  // <=== loginFormTest 함수 호출시 실제로 호출할 가상 주소 설정
	public ModelAndView loginFormTest() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("loginForm");
		return mav;
	}
	
	/**
	 * 비동기식 호출 이기 때문에 ModelAndView가 아닌 int로 return한다
	 * 가상주소 /erp/loginProc.do로 접속하면 호출되는 메소드 선언.
	 * [컨트롤러 클래스]의 메소드에 @ResponseBody가 있고, @RequestMapping이 붙으면
	 * 메소드의 리턴 데이터가 바로 클라이언트에게 전송된다.
	 * 클라이언트는 JSON형태로 받게된다.
	 */
	@RequestMapping(
			value    = "/loginProc.do"
		  , method   = RequestMethod.POST               /* get방식의 접근을 막고 post방식의 접근만 허용 */
		  , produces = "application/json;charset=UTF-8" /* return값을 json방식으로 return하기 위한 설정 */
    )
	@ResponseBody /* return값을 json방식이나 string(int)등 html방식이 아닌 값으로 return할때는 @ResponseBody설정이 필요 하다. */
	public int loginProc(
//			@RequestParam(value = "admin_id") String admin_id
//		  , @RequestParam(value = "pwd"     ) String pwd
			@RequestParam Map<String, String> map   /* 이구문은 jsp에서 넘어오는 값들이 String값처럼 단순한 값일 때문 사용가능, checkbox처럼 여러값이 넘어올 때는 사용 불가능 */
	) 
	{  /* public @ResponseBody int loginProc() {   <=== @ResponseBody는 이런식으로 설정 해도 된다. */
		// 서비스클래스에게 DB연동을 수주 준다.
		int adminCnt = 0;
		
		try {
			/**
			 * HashMap 객체에 [로그인 아이디, 암호] 저장하기
			 */
//			Map<String, String> admin_id_pwd = new HashMap<String, String>();
//			admin_id_pwd.put("admin_id", admin_id);  // ID
//			admin_id_pwd.put("pwd"     , pwd     );  // PWD
			
			/**
			 * this.를 붙이는 이유는 호출하는 메소드가 상속받은 쪽에 존재 할수도 있기 때문에 혼동을 피하기위해서 사용
			 */
//			adminCnt = this.loginService.getAdminCnt(admin_id_pwd);
			adminCnt = this.loginService.getAdminCnt(map);
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("LoginController.loginProc(~) 에서 에러발생");
			adminCnt = -1;
		}
		
		return adminCnt;
	}
	
	@RequestMapping(
			value    = "/loginProcTest.do"
		  , method   = RequestMethod.POST               /* get방식의 접근을 막고 post방식의 접근만 허용 */
    )
	@ResponseBody /* return값을 json방식이나 string(int)등 html방식이 아닌 값으로 return할때는 @ResponseBody설정이 필요 하다. */
	public ModelAndView loginProcTest(
			@RequestParam Map<String, String> map
	) 
	{  /* public @ResponseBody int loginProc() {   <=== @ResponseBody는 이런식으로 설정 해도 된다. */
		// 서비스클래스에게 DB연동을 수주 준다.
		int adminCnt = 0;
		ModelAndView mav = new ModelAndView();
		mav.setViewName("loginProc");
		
		try {
			/**
			 * HashMap 객체에 [로그인 아이디, 암호] 저장하기
			 */
//			Map<String, String> admin_id_pwd = new HashMap<String, String>();
//			admin_id_pwd.put("admin_id", admin_id);  // ID
//			admin_id_pwd.put("pwd"     , pwd     );  // PWD
			/**
			 * this.를 붙이는 이유는 호출하는 메소드가 상속받은 쪽에 존재 할수도 있기 때문에 혼동을 피하기위해서 사용
			 */
//			adminCnt = this.loginService.getAdminCnt(admin_id_pwd);
			adminCnt = this.loginService.getAdminCnt(map);
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("LoginController.loginProc(~) 에서 에러발생");
			adminCnt = -1;
		}
		
		mav.addObject("adminCnt", adminCnt);
		return mav;
	}
}
