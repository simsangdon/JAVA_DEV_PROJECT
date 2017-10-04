<%@page import="java.util.Iterator"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<% pageContext.setAttribute("newLineChar", "\n"); %>
<%@ include file="/WEB-INF/include/include-header.jspf" %>
<%@ page import= "java.io.ByteArrayOutputStream" %>
<%@ page import= "java.io.ByteArrayInputStream" %>
<%@ page import= "java.io.ObjectInputStream" %>
<%@ page import= "java.io.ObjectOutputStream" %>
<%@ page import= "java.util.HashMap" %>
<%@ page import= "java.util.Map" %>
<%@ page import= "java.util.Iterator" %>
<%@ page import= "sun.misc.BASE64Encoder" %>
<%@ page import= "sun.misc.BASE64Decoder" %>
<%
//     String streamStr = request.getParameter("streamStr");
//     System.out.println("streamStr : " + streamStr);

//     byte[] rbuf = new BASE64Decoder().decodeBuffer(streamStr);
//     if (rbuf != null) {                                                                                 
//         ObjectInputStream objectIn = new ObjectInputStream( new ByteArrayInputStream(rbuf));            
//         Object obj = objectIn.readObject(); // Contains the object
//         Map<String, Object> rtnMap1 = (Map<String, Object>)obj;
//         Map<String, String> tmpMap  = null;
        
//         Iterator<String> tmpItr = rtnMap1.keySet().iterator();
//         String kekStr           = "";
//         Object tmpObj           = null;
        
//         Iterator<String> tmpItr1 = null;
//         String kekStr1           = "";
        
//         while(tmpItr.hasNext()) {
//             kekStr = tmpItr.next();
//             tmpObj = rtnMap1.get(kekStr); 
//             if(tmpObj instanceof Map) {
//                 tmpMap = (Map<String, String>)tmpObj;
//                 tmpItr1 = tmpMap.keySet().iterator();
                
//                 while(tmpItr1.hasNext()) {
//                     kekStr1 = tmpItr1.next();
//                     System.out.println(kekStr1 + " : " + tmpMap.get(kekStr1));
//                 }
                
//             } else {
//                 System.out.println(kekStr + " : " + tmpObj.toString());    
//             }
            
//         }
//     } 

%>
</head>
<body>
    <table class="board_view">
        <colgroup>
            <col width="15%"/>
            <col width="35%"/>
            <col width="15%"/>
            <col width="35%"/>
        </colgroup>
        <caption>게시글 상세</caption>
        <tbody>
            <tr>
                <th scope="row">글 번호</th>
                <td>${map.IDX }</td>
                <th scope="row">조회수</th>
                <td>${map.HIT_CNT }</td>
            </tr>
            <tr>
                <th scope="row">작성자</th>
                <td>${map.CREA_ID }</td>
                <th scope="row">작성시간</th>
                <td>${map.CREA_DTM }</td>
            </tr>
            <tr>
                <th scope="row">제목</th>
                <td colspan="3">${map.TITLE }</td>
            </tr>
            <tr>
                <td colspan="4">${fn:replace(map.CONTENTS, newLineChar, "<br/>")}</td>
            </tr>
            <tr>
                <th scope="row">첨부파일</th>
                <td colspan="3">
                    <c:forEach var="row" items="${list }">
                        <input type="hidden" id="IDX" value="${row.IDX }">
                        <a href="#this" name="file">${row.ORIGINAL_FILE_NAME }</a> 
                        (${row.FILE_SIZE }kb)
                    </c:forEach>
                </td>
            </tr>
        </tbody>
    </table>
    <br/>
     
     
    <a href="#this" class="btn" id="list">목록으로</a>
    <a href="#this" class="btn" id="update">수정하기</a>
     
    <%@ include file="/WEB-INF/include/include-body.jspf" %>
    <script type="text/javascript">
        $(document).ready(function(){
            $("#list").on("click", function(e){ //목록으로 버튼
                e.preventDefault();
                fn_openBoardList();
            });
             
            $("#update").on("click", function(e){ //수정하기 버튼
                e.preventDefault();
                fn_openBoardUpdate();
            });
             
            $("a[name='file']").on("click", function(e){ //파일 이름
                e.preventDefault();
                fn_downloadFile($(this));
            });
        });
         
        function fn_openBoardList(){
            var comSubmit = new ComSubmit();
            comSubmit.setUrl("<c:url value='/sample/openBoardList.do' />");
            comSubmit.submit();
        }
         
        function fn_openBoardUpdate(){
            var idx = "${map.IDX}";
            var comSubmit = new ComSubmit();
            comSubmit.setUrl("<c:url value='/sample/openBoardUpdate.do' />");
            comSubmit.addParam("IDX", idx);
            comSubmit.submit();
        }
        
        function fn_downloadFile(obj){
            var idx = obj.parent().find("#IDX").val();
            var comSubmit = new ComSubmit();
            comSubmit.setUrl("<c:url value='/common/downloadFile.do' />");
            comSubmit.addParam("IDX", idx);
            comSubmit.submit();
        }        
    </script>
</body>
</html>