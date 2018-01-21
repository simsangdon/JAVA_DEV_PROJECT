
//**************************************************************
// body 태그 안의 form 태그, input 태그, select 태그, textarea 태그의 class="name 속성값" 을 삽입하기
//**************************************************************
function addClassN( ){
	$("body").find("input,select,textarea,form").each(function(){
		var nameV = $(this).attr("name");
		if(nameV!=null  && nameV!=undefined && nameV!=""){
			$(this).addClass(nameV);
		}
	});
}

//**************************************************************
// 검색화면에서 검색 결과물의 페이징 번호 출력 소스 리턴
//**************************************************************
function printPagingHtml(
	totRowCnt                  // 검색 결과 총 행 개수
	, selectPageNo_str         // 선택된 현재 페이지 번호
	, rowCntPerPage_str     // 페이지 당 출력행의 개수
	, pageNoCntPerPage_str  // 페이지 당 출력번호 개수
	, jsCodeAfterClick      // 페이지 번호 클릭후 실행할 자스 코드
) {
	var arr = [];
	try{
		if( totRowCnt==0
			|| selectPageNo_str==null || selectPageNo_str.length==0
			|| rowCntPerPage_str==null || rowCntPerPage_str.length==0
			|| pageNoCntPerPage_str==null || pageNoCntPerPage_str.length==0
			|| jsCodeAfterClick==null || jsCodeAfterClick.length==0
		){
			return;
		}				
		//alert(99);
		//--------------------------------------------------------------
		// 페이징 처리 관련 데이터 얻기
		//--------------------------------------------------------------
		if( selectPageNo_str==null || selectPageNo_str.length==0 )              { 
			selectPageNo_str="1";  // 선택한 현재 페이지 번호 저장
		} 
		if( rowCntPerPage_str==null || rowCntPerPage_str.length==0 )      { 
			rowCntPerPage_str="10";  // 선택한 현재 페이지 번호 저장
		}
		if( pageNoCntPerPage_str==null || pageNoCntPerPage_str.length==0 ) { 
			pageNoCntPerPage_str="10";  // 선택한 현재 페이지 번호 저장
		}
		//---
		var selectPageNo = parseInt(selectPageNo_str, 10);
		var rowCntPerPage = parseInt(rowCntPerPage_str,10);
		var pageNoCntPerPage = parseInt(pageNoCntPerPage_str,10);
		if( rowCntPerPage<=0 || pageNoCntPerPage<=0 ) { return; }
		//-------------------------------------
		//최대 페이지 번호 얻기
		var maxPageNo=Math.ceil( totRowCnt/rowCntPerPage );   
			if( maxPageNo<selectPageNo ) { selectPageNo = 1; }
		// 선택된 페이지번호에 따라 출력할 [시작 페이지 번호] 얻기
		var startPageNo = Math.floor((selectPageNo-1)/pageNoCntPerPage)*pageNoCntPerPage+1;  // 시작 페이지 번호
		// 선택된 페이지번호에 따라 출력할 [끝 페이지 번호] 얻기
		var endPageNo = startPageNo+pageNoCntPerPage-1;
			if( endPageNo>maxPageNo ) { endPageNo=maxPageNo; }
		//--------------------------------------------------------------
		// name=nowPage을 가진 hidden 태그없으면 경고하고 중지하는 자바스크립트 소스 생성해 저장
		//--------------------------------------------------------------
		$(document).ready(function( ){
			if( $('[name=selectPageNo]').length==0 ){
				alert("name=nowPage 을 가진 hidden 태그가 있어야 "
					 +"com.naver.common.Util 클래스의 getPagingHtml(~) 메소드 호출이 가능함.');" );
			}
		});
		//---
		var cursor = " style='cursor:hand' ";
		//arr.push( "<table border=0 cellpadding=3 style='font-size:13'  align=center> <tr>" );
		//--------------------------------------------------------------
		// [처음] [이전] 출력하는 자바스크립트 소스 생성해 저장
		//--------------------------------------------------------------
		//arr.push( "<td align=right width=110>&nbsp;" );
		if( startPageNo>pageNoCntPerPage ) {
			arr.push( "<span "+cursor+" onclick=\"$('[name=selectPageNo]').val('1');"
							+jsCodeAfterClick+";\">[처음]</span>" );
			arr.push( "<span "+cursor+" onclick=\"$('[name=selectPageNo]').val('"
				+(startPageNo-1)+"');"+jsCodeAfterClick+";\">[이전]</span>&nbsp;&nbsp;&nbsp;" );
		}
		//--------------------------------------------------------------
		// 페이지 번호 출력하는 자바스크립트 소스 생성해 저장
		//--------------------------------------------------------------
		//arr.push( "<td align=center>&nbsp;&nbsp;" );
		for( var i=startPageNo ; i<=endPageNo; ++i ){
			if(i>maxPageNo) {break;}
			if(i==selectPageNo || maxPageNo==1 ) {
				arr.push( "<b>"+i +"</b> " );
			}else{
				arr.push( "<span "+cursor+" onclick=\"$('[name=selectPageNo]').val('"
							+(i)+"');"+jsCodeAfterClick+";\">["+i+"]</span> " );
			}
		}
		//--------------------------------------------------------------
		// [다음] [마지막] 출력하는 자바스크립트 소스 생성해 저장
		//--------------------------------------------------------------
		//arr.push( "<td align=left width=110>&nbsp;&nbsp;" );
		if( endPageNo<maxPageNo ) {
			arr.push( "&nbsp;&nbsp;&nbsp;<span "+cursor+" onclick=\"$('[name=selectPageNo]').val('"
						+(endPageNo+1)+"');"+jsCodeAfterClick+";\">[다음]</span>" );
			arr.push( "<span "+cursor+" onclick=\"$('[name=selectPageNo]').val('"
						+(maxPageNo)+"');"+jsCodeAfterClick+";\">[마지막]</span>" );
		}
		//arr.push( "</table>" );
		return arr.join( "" );
	}catch(ex){
		alert("printPagingHtml(~) 메소드 호출 시 예외발생!");
		return "";
	}
}


//**************************************************************
// 검색화면에서 검색 결과물의 페이징 번호 출력 소스 리턴
//**************************************************************
function printPagingHtml2(
	totRowCnt               // 검색 결과 총 행 개수
	, selectPageNo_str         // 선택된 현재 페이지 번호
	, rowCntPerPage_str     // 페이지 당 출력행의 개수
	, pageNoCntPerPage_str  // 페이지 당 출력번호 개수
	, jsCodeAfterClick      // 페이지 번호 클릭후 실행할 자스 코드
) {
	var arr = [];
	try{
		if( totRowCnt==0 ){	return ""; }	
		if( jsCodeAfterClick==null || jsCodeAfterClick.length==0){
			alert("printPagingHtml2(~) 함수의 5번째 인자는 존재하는 함수명이 와야 합니다");
			return "";
		}			
		//--------------------------------------------------------------
		// 페이징 처리 관련 데이터 얻기
		//--------------------------------------------------------------
		if( selectPageNo_str==null || selectPageNo_str.length==0 ) { 
			selectPageNo_str="1";  // 선택한 현재 페이지 번호 저장
		} 
		if( rowCntPerPage_str==null || rowCntPerPage_str.length==0 ) { 
			rowCntPerPage_str="10";  // 선택한 현재 페이지 번호 저장
		}
		if( pageNoCntPerPage_str==null || pageNoCntPerPage_str.length==0 ) { 
			pageNoCntPerPage_str="10";  // 선택한 현재 페이지 번호 저장
		}
		//---
		var selectPageNo = parseInt(selectPageNo_str, 10);
		var rowCntPerPage = parseInt(rowCntPerPage_str,10);
		var pageNoCntPerPage = parseInt(pageNoCntPerPage_str,10);
		if( rowCntPerPage<=0 || pageNoCntPerPage<=0 ) { return; }
		//--------------------------------------------------------------
		//최대 페이지 번호 얻기
		//--------------------------------------------------------------
		var maxPageNo=Math.ceil( totRowCnt/rowCntPerPage );   
			if( maxPageNo<selectPageNo ) { selectPageNo = 1; }
		//--------------------------------------------------------------
		// 선택된 페이지번호에 따라 출력할 [시작 페이지 번호] 얻기
		//--------------------------------------------------------------
		var startPageNo = Math.floor((selectPageNo-1)/pageNoCntPerPage)*pageNoCntPerPage+1;  // 시작 페이지 번호
		//--------------------------------------------------------------
		// 선택된 페이지번호에 따라 출력할 [끝 페이지 번호] 얻기
		//--------------------------------------------------------------
		var endPageNo = startPageNo+pageNoCntPerPage-1;
			if( endPageNo>maxPageNo ) { endPageNo=maxPageNo; }
		//--------------------------------------------------------------
		// name=nowPage을 가진 hidden 태그없으면 경고하고 중지하는 자바스크립트 소스 생성해 저장
		//--------------------------------------------------------------
		$(document).ready(function( ){
			if( $('[name=selectPageNo]').length==0 ){
				alert("name=nowPage 을 가진 hidden 태그가 있어야 가능함.');" );
			}
		});
		//---
		var cursor = " style='cursor:pointer' ";
		//arr.push( "<table border=0 cellpadding=3 style='font-size:13'  align=center> <tr>" );
		//--------------------------------------------------------------
		// [처음] [이전] 출력하는 자바스크립트 소스 생성해 저장
		//--------------------------------------------------------------
		//arr.push( "<td align=right width=110>&nbsp;" );
		if( startPageNo>pageNoCntPerPage ) {
			arr.push( "<span "+cursor+" onclick=\"$('[name=selectPageNo]').val('1');"
							+jsCodeAfterClick+";\">[처음]</span>" );
			arr.push( "<span "+cursor+" onclick=\"$('[name=selectPageNo]').val('"
				+(startPageNo-1)+"');"+jsCodeAfterClick+";\">[이전]</span>&nbsp;&nbsp;&nbsp;" );
		}
		//--------------------------------------------------------------
		// 페이지 번호 출력하는 자바스크립트 소스 생성해 저장
		//--------------------------------------------------------------
		//arr.push( "<td align=center>&nbsp;&nbsp;" );
		for( var i=startPageNo ; i<=endPageNo; ++i ){
			if(i>maxPageNo) {break;}
			if(i==selectPageNo || maxPageNo==1 ) {
				arr.push( "<b>"+i +"</b> " );
			}else{
				arr.push( "<span "+cursor+" onclick=\"$('[name=selectPageNo]').val('"
							+(i)+"');"+jsCodeAfterClick+";\">["+i+"]</span> " );
			}
		}
		//--------------------------------------------------------------
		// [다음] [마지막] 출력하는 자바스크립트 소스 생성해 저장
		//--------------------------------------------------------------
		//arr.push( "<td align=left width=110>&nbsp;&nbsp;" );
		if( endPageNo<maxPageNo ) {
			arr.push( "&nbsp;&nbsp;&nbsp;<span "+cursor+" onclick=\"$('[name=selectPageNo]').val('"
						+(endPageNo+1)+"');"+jsCodeAfterClick+";\">[다음]</span>" );
			arr.push( "<span "+cursor+" onclick=\"$('[name=selectPageNo]').val('"
						+(maxPageNo)+"');"+jsCodeAfterClick+";\">[마지막]</span>" );
		}
		//arr.push( "</table>" );
		return arr.join( "" );
	}catch(ex){
		alert("printPagingHtml2(~) 메소드 호출 시 예외발생!");
		return "";
	}
}


//**************************************************************
function setTableTrBgColor( tableClassV, oddBgColor, evenBgColor, mouceOnBgColor ) {
//**************************************************************
	//alert(2);
	//var trObjs = $("."+tableClassV+" tr:eq(0)").parent().children().eq(0).nextAll();
	var trObjs = $("."+tableClassV+" tr:eq(0)").nextAll();
	//--------------------------------
	//기존 배경색 CSS 제거
	//--------------------------------
	trObjs.css('background', "none" );

	//--------------------------------
	//새로운 배경색 CSS 지정
	//--------------------------------
	trObjs.filter(":odd").css('background', evenBgColor );
	trObjs.filter(":even").css('background', oddBgColor );
	//----------------------
	trObjs.hover(
		function(){   
			$(this).find("td").css('background', mouceOnBgColor );
			/*$(this).find("td").each(function(){
				$(this).html("<div class='xxx'>"+$(this).text()+"</div>");
				alert($(this).html());
			});
			showHideLoop( "xxx", 1000 );*/
		}, 
		function(){   
			//alert($(this).index())
			if( $(this).index()%2==0 ) {
				$(this).find("td").css('background', evenBgColor );
				
			}else{
				$(this).find("td").css('background', oddBgColor );
			}

			/*$(this).find("td").each(function(){
				$(this).html($(this).text());
			});*/
		}
	); 
	//lert(2)
}


//**************************************************************
// class=xxx 가진 영력을 보였다 안보였다 반복하기
//**************************************************************
function showHideLoop( classN, second ){
	$("."+classN).fadeOut(second, function(){ $("."+classN).fadeIn(); showHideLoop( classN ); } );
}



/*

//**************************************************************
//검색화면에서 검색 결과물의 페이징 번호 출력 소스 리턴
//**************************************************************
function openClass(){
	$(".xxx").html(
		"<div "+
		"id='glass' onclick='alert('[입력 화면] 또는 [수정/삭제 화면]을 닫아야 [검색 화면]을 클릭할수 있습니다.')'"
		+"style='z-index:1;position:absolute;left:0;top:0;display:none;"
		+"filter:Alpha(Opacity='30');opacity:0.3;width:100%;height:100%;background-color:gray'>"
		+"</div>"
	);
	$(".glass").show();
	
}
//**************************************************************
function setTableTrBgColor( tableClassV, oddBgColor, evenBgColor, mouceOnBgColor ) {
//**************************************************************
	// id="staff" 가 있는 태그 후손의 <thead>안의 후손의 tr 요소들에 지정한 css 적용
	$("."+tableClassV+" thead tr").css('background', '#8A8AFF');
	// id="staff" 가 있는 태그 후손의 <tbody>안의 후손의 tr 중 [인덱스번호]가 
	//[홀수번째] tr 요소들에 지정한 css 적용
	$("."+tableClassV+" tbody tr:odd").css('background', '#AAAAAA');
	// id="staff" 가 있는 태그 후손의 <tbody>태그 후손의 tr 중 [인덱스번호]가 
	//[짝수번째] tr 요소들에 지정한 css 적용
	$("."+tableClassV+" tbody tr:even").css('background', '#FFFFFF');

	//$(선택자).hover( function (){코드1}, function (){코드2} ) 
	// => 선택자에 마우스 대면 코드1 실행, 띠면 코드2 실행.
	//-----------------------------------------------------
	// id="staff" 가 있는 태그 후손의 <tbody>태그 후손의 각 tr 에 마우스 대면
	// 설정한 배경색으로 바꾸고 띠면 원래로 돌아가도록 설정
	//-----------------------------------------------------
	$("."+tableClassV).find("tbody").find("tr").hover(
		//$("."+tableClassV+" tbody tr").hover(
		function(){   
			// 마우스를 갖다댄 tr 태그 후손의 td 태그에 class="style1" 삽입함.
			// class="style1" 삽입함으로서 <style> 태그에 설정한 CSS 가 적용됨.
			// this 는 마우스를 갖다댄 tr 태그
			$(this).find("td").addClass('style1'); 
		}, 
		function(){   
			//마우스를 뺀 tr 태그 안에 td 태그에 class="style1" 제거
			$(this).find("td").removeClass('style1');   
		}
	);  
	// id="staff" 가 있는 태그 후손의 <tbody>태그 후손의 각 tr 에 마우스 대면
	// 설정한 배경색으로 바꾸고 띠면 원래로 돌아가도록 설정
	$("."+tableClassV+" thead th").hover(
		function( ){  
			// 마우스가 올라간 th 태그의 형제 요소 중 순서번호 저장 
			var no=$(this).index()+1;          
				//var no=$(this).parent( ).children( ).index(this)+1;  도 가능                      
			// tbody 내부의 td 중에 각 td 가 속한 부모 태그의 no 번째 자식 td 태그를 골라
			// class=style1 를 삽입
			$("."+tableClassV+" tbody td:nth-child('+no+')").addClass('style1');
			//$("."+tableClassV+" tbody td:eq('+no+')").addClass('style1');
		}, 
		function( ){   
			// 마우스를 때면 class="style1"  속성을 제거
			$("."+tableClassV+" tbody tr").children( ).removeClass('style1');
			//$("."+tableClassV+" tbody td").removeClass('style1'); 도 가능
		}
	);  
}
*/







