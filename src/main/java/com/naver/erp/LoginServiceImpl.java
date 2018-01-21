package com.naver.erp;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Controller, @Service, @Repository 가 붙어 있는 것들은 자동으로 bean테그로 등록해 준다(servlet-context.xml의 <context:component-scan base-package="com.naver.erp" />)
 */
@Service
@Transactional
public class LoginServiceImpl implements LoginService{
	/**
	 * 속성변수 loginDAO 선언하고, LoginDAO 라는 인터페이스 를
	 * 구현한 클래스를 객체화하여 저장
	 * @Autowired 이 붙은 속성변수에는 인테페이스 자료형을 쓰고
	 * 이 인터페이스를 구현한 클래스를 객체화하여 저장한다.
	 * LoginDAO 라는 인터페이스를 구현한 클래스의 이름을 몰라도 관계 없다.
	 * 1개 존재하기만 하면 된다(1개이상이면 에러난다.)
	 */
	@Autowired
	private LoginDAO loginDAO;
	
	/**
	 * 로그인 정보의 개수를 리턴하는 메소드 선언
	 */
	public int getAdminCnt(Map<String, String> admin_id_pwd) {
		int adminCnt = this.loginDAO.getAdminCnt(admin_id_pwd);
		return adminCnt;
	}
}
