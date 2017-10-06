package first.common.util.file;

import java.io.File;

public class fileTest {

	public static void main(String[] args) throws Exception {
		
		fileUtil fUtil = new fileUtil();
		String inFilePath  = "src\\main\\java\\first\\common\\util\\file\\testFile\\testFile1-UTF-8";
		//System.out.println(fUtil.readFile(filePath));
		
		File inFile  = new File(inFilePath);
		String encoding = "UTF-8";
		
		if(inFile.isDirectory())
		{
			fUtil.iterateDirectory(inFile, encoding);
		}
		else
		{
			fUtil.convertEncoding(inFile, "");
		}
	}
}