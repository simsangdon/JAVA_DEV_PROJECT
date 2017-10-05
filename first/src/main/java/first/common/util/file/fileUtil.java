package first.common.util.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class fileUtil {

	/* 라인별 파일 읽는 메소드 */
	public int readFile(String filePath) {
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		
		try 
		{
			br = new BufferedReader(new FileReader(filePath));
			String line = null;
			
			while((line = br.readLine()) != null)
			{
				System.out.println(line);
				sb.append(line);
			}
		} 
		catch (IOException ioe) 
		{
			System.out.println(ioe.getMessage());
		} 
		finally 
		{
			try
			{
				if(br != null) 
				{
					br.close();
				}
			}
			catch (Exception e)
			{
				e.getMessage();
			}
		}
		
		return sb.length();
	}
}
