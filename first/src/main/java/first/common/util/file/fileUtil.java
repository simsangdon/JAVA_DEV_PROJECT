package first.common.util.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.mozilla.universalchardet.UniversalDetector;

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
	public void iterateDirectory(File file) 
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
					iterateDirectory(resFile);
				}
				else
				{
					// �ش� ��尡 �����̸� ���� ó�� �κ� ȣ��
					convertEncoding(resFile);
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
	@SuppressWarnings("unchecked")
	public void convertEncoding(File file) throws Exception 
	{	
		// FileInputStream���(file encodingȮ��)
		try
		{
			byte[] buf = new byte[4096];
			@SuppressWarnings("resource")
			FileInputStream fis = new FileInputStream(file);
			UniversalDetector detector = new UniversalDetector(null);
			int nread;
			
			while((nread = fis.read(buf)) > 0 && !detector.isDone())
			{
				detector.handleData(buf, 0, nread);
			}
			detector.dataEnd();
			
			String rtnEncoding = detector.getDetectedCharset();
			
			if(rtnEncoding != null)
			{
				System.out.println("Detected encoding : " + rtnEncoding);
			}
			else
			{
				System.out.println("No encoding detected.");
			}
			detector.reset();
			
			File outFile = new File(file.getPath() + "_OUT");
			outFile.delete();
			outFile.createNewFile();
			
			//decodinigFileBufferReader(file, outFile, rtnEncoding);
			decodingFile(file, outFile, rtnEncoding);
			//decodingFileInputStream(file, outFile, rtnEncoding);
		}
		catch (IOException ioe)
		{
			ioe.getMessage();
			ioe.getStackTrace();
			throw ioe;
		}
	}
	
	/* encoding���� ��ȯ�� STRING ���Ͽ� ����(BufferReader���) */
	@SuppressWarnings("unchecked")
	public void decodinigFileBufferReader(File file, File outFile, String encoding) 
	{
		// BufferReader���
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(file));
			String read = null;
			@SuppressWarnings("rawtypes")
			List list = new ArrayList();
			
			while((read = in.readLine()) != null)
			{
				list.add(read);
			}
			in.close();
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), encoding));
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
	
	/* encoding���� ���Ͽ� ���� */
	@SuppressWarnings("resource")
	public void decodingFile(File file, File outFile, String encoding) 
	{
		try
		{
			Charset charset = Charset.forName(encoding);
			FileInputStream fis = new FileInputStream(file);
			//InputStreamReader inputStream = new InputStreamReader(fis, encoding);
			ByteArrayOutputStream fbs = new ByteArrayOutputStream();
			
			byte[] buffer = new byte[4096];
			int n = 0;
			
			while((n = fis.read(buffer, 0, buffer.length)) > 0)
			{
				fbs.write(buffer, 0, n);
			}
			
			CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(fbs.toString().getBytes()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
			bw.append(charBuffer);
			bw.close();
		}
		catch(IOException ioe)
		{
			ioe.getMessage();
			ioe.getStackTrace();
		}
	}
	
	/* encodingŸ������ ������ �о ���� */
	@SuppressWarnings("resource")
	public void decodingFileInputStream(File file, File outFile, String encoding) throws Exception
	{
		System.out.println("encoding : " + encoding);
		try
		{
			FileInputStream fis = new FileInputStream(file);
			BufferedReader brd = new BufferedReader(new InputStreamReader(fis, encoding));
			String fileString = null;
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), encoding));
			
			char[] charBuffer = new char[4096];
			brd.read(charBuffer, 0, 6);
			out.write(charBuffer, 0, 6);
			brd.read(charBuffer, 6, 6);
			out.write(charBuffer, 6, 6);
			
			while((fileString = brd.readLine()) != null)
			{
				//System.out.println("fileString : " + fileString);
				out.write(fileString);
				out.newLine();
			}
			out.close();
		}
		catch(IOException ioe)
		{
			ioe.getMessage();
			ioe.getStackTrace();
			throw ioe;
		}
		catch(Exception e)
		{
			e.getMessage();
			e.getStackTrace();
			throw e;
		}
	}
}
