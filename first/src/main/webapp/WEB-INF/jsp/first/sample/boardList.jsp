<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<title>first</title>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/include/include-header.jspf" %>
<%@ page import= "java.io.ByteArrayOutputStream" %>
<%@ page import= "java.io.ByteArrayInputStream" %>
<%@ page import= "java.io.ObjectInputStream" %>
<%@ page import= "java.io.ObjectOutputStream" %>
<%@ page import= "java.util.HashMap" %>
<%@ page import= "java.util.Map" %>
<%@ page import= "sun.misc.BASE64Encoder" %>
<%@ page import= "sun.misc.BASE64Decoder" %>
<%
    ByteArrayOutputStream bos = null;
    ObjectOutputStream oos = null;
    ObjectInputStream ois = null;
    Map<String,Object> tmpMap = new HashMap<String, Object>();
    Map<String,String> tmpMap1 = new HashMap<String, String>();
    
    tmpMap.put("aaa", "111");
    tmpMap.put("bbb", "222");
    tmpMap.put("ccc", "333");
    
    tmpMap1.put("AAA", "555");
    tmpMap1.put("BBB", "666");
    tmpMap1.put("CCC", "777");
    
    tmpMap.put("ddd", tmpMap1);
    
    bos = new ByteArrayOutputStream();
    oos = new ObjectOutputStream(bos);
    oos.writeObject(tmpMap);
    String rtnMap = bos.toString();
    
    System.out.println("rtnMap : " + rtnMap);
    
    byte[] buf = bos.toByteArray();
    String streamStr = new BASE64Encoder().encode(buf);
    request.setAttribute("streamStr", streamStr);
    
    
    System.out.println("rtnMap______ : " + rtnMap);
    System.out.println("rtnMap______ : " + rtnMap);
    
%>
</head>
<body>
<h2>게시판 목록</h2>
    <form id="frm">
<%--         <input type="text" id="streamStr" name="streamStr" value="${streamStr }" /> --%>
        <table style="border:1px solid #ccc">
            <colgroup>
                <col width="10%"/>
                <col width="*"/>
                <col width="15%"/>
                <col width="20%"/>
            </colgroup>
            <thead>
                <tr>
                    <th scope="col">글번호</th>
                    <th scope="col">제목</th>
                    <th scope="col">조회수</th>
                    <th scope="col">작성일</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${fn:length(list) > 0}">
                        <c:forEach items="${list }" var="row">
                            <tr>
                                <td>${row.IDX }</td>
                                <td class="title">
                                    <a href="#this" name="title">${row.TITLE }</a>
                                    <input type="hidden" id="IDX" value="${row.IDX }">
                                </td>
                                <td>${row.HIT_CNT }</td>
                                <td>${row.CREA_DTM }</td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="4">조회된 결과가 없습니다.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
        <a href="#this" class="btn" id="write" >글쓰기</a>
    </form>    
    <%@ include file="/WEB-INF/include/include-body.jspf" %>
    <script type="text/javascript">
        $(document).ready(function(){
            $("#write").on("click", function(e){ //작성하기 버튼
                e.preventDefault();
                fn_insertBoard();
            });
            $("a[name='title']").on("click", function(e){ //제목 
                e.preventDefault();
                fn_openBoardDetail($(this));
            });            
        });
         
        function fn_insertBoard(){
            var comSubmit = new ComSubmit();
            comSubmit.setUrl("<c:url value='/sample/openBoardWrite.do' />");
            comSubmit.submit();
        }
        function fn_openBoardDetail(obj){
            var comSubmit = new ComSubmit();
            comSubmit.setUrl("<c:url value='/sample/openBoardDetail.do' />");
            comSubmit.addParam("IDX", obj.parent().find("#IDX").val());
            comSubmit.addParam("streamStr", $("#streamStr").val());
            comSubmit.submit();
        }    
    </script>  
</body>
</html>