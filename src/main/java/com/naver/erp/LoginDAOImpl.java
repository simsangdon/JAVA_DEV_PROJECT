package com.naver.erp;

import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @Repository 를 붙임으로써 [DAO 클래스] 임을 지정하게되고, bean 태그로 자장 등록된다.
 * @Controller, @Service, @Repository 가 붙어 있는 것들은 자동으로 bean테그로 등록해 준다(servlet-context.xml의 <context:component-scan base-package="com.naver.erp" />)
 */
@Repository
public class LoginDAOImpl implements LoginDAO{
	/**
	 * SqlSessionTemplate 객체를 생성해 속성변수 sqlSession저장
	 */
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	public int getAdminCnt(Map<String, String> admin_id_pwd) {
		int adminCnt = this.sqlSession.selectOne(
				"com.naver.erp.LoginDAO.getAdminCnt"  /* xml파일의 sql아이디(xml파일의 namespace값) */
			   , admin_id_pwd);                       /* sql실행 파라메터                           */
		return adminCnt;
	}
}
