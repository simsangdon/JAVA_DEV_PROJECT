package first.common.util.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class fileUtil {

	/*
	 * ���κ� ���� �д� �޼ҵ�
	 */
	public int readFile(String filePath) 
	{
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
	
	/*
	 * ��� ��尡 ������ ���
	 */
	public void iterateDirectory(File file, String encoding) 
	{
		try
		{
			String path = file.getCanonicalPath();
			String [] str = file.list();
			File resFile = null;
			
			for (String fileName : str) 
			{
				resFile = new File(path + "/" + fileName);
				System.out.println("filePath : " + path + "/" + fileName);
				if(resFile.isDirectory())
				{
					// �ش� ��尡 �����̸� ����ȣ��
					iterateDirectory(resFile, encoding);
				}
				else
				{
					// �ش� ��尡 �����̸� ���� ó�� �κ� ȣ��
					convertEncoding(resFile, encoding);
				}
			}
		}
		catch (Exception e)
		{
			e.getMessage();
			e.printStackTrace();
		}
	}
	
	/*
	 * ��� ��尡 ������ ���
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void convertEncoding(File file, String encoding) 
	{
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(file));
			String read = null;
			List list = new ArrayList();
			
			while((read = in.readLine()) != null)
			{
				list.add(read);
			}
			in.close();
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
			for (Object object : list) {
				out.write((String)object);
				out.newLine();
			}
			out.close();
		}
		catch (IOException ioe)
		{
			ioe.getMessage();
			ioe.getStackTrace();
		}
	}
}
