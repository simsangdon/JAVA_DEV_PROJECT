package com.naver.erp;

import java.util.Map;

public interface LoginService {
	/**
	 * 인터페이스는 컴파일될때 public, abstract 두가지 속성이 자동적으로 셋팅 된다.
	 * [로그인 아이디, 암호 존재 개수] 검색 메소드 선언
	 */
	int getAdminCnt(Map<String, String> admin_id_pwd);
}
