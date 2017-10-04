package first.sample.dao;
 
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import first.common.common.CommandMap;
import first.common.dao.AbstractDAO;
 
@Repository("sampleDAO")
public class SampleDAO extends AbstractDAO{

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectBoardList(CommandMap commandMap) {
		// TODO Auto-generated method stub
		return (List<Map<String, Object>>)selectList("sample.selectBoardList", commandMap);
	}
	
	public void insertBoard(Map<String, Object> map) throws Exception{
	    insert("sample.insertBoard", map);
	}
	
	public void updateHitCnt(Map<String, Object> map) throws Exception{
	    update("sample.updateHitCnt", map);
	}
	 
	@SuppressWarnings("unchecked")
	public Map<String, Object> selectBoardDetail(Map<String, Object> map) throws Exception{
	    return (Map<String, Object>) selectOne("sample.selectBoardDetail", map);
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectFileList(Map<String, Object> map) throws Exception{
	    return (List<Map<String, Object>>)selectList("sample.selectFileList", map);
	}	
	
	public void updateBoard(Map<String, Object> map) throws Exception{
	    update("sample.updateBoard", map);
	}
 
	public void deleteBoard(Map<String, Object> map) throws Exception{
	    update("sample.deleteBoard", map);
	}
	
	public void insertFile(Map<String, Object> map) throws Exception{
	    insert("sample.insertFile", map);
	}
}