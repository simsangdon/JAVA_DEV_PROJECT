package first.common.util.file;

import java.io.File;

public class fileTest {

	public static void main(String[] args) throws Exception {
		
		fileUtil fUtil = new fileUtil();
		//String inFilePath  = "src\\main\\java\\first\\common\\util\\file\\testFile\\TEST_FILE_EUC_KR";
		String inFilePath  = "src\\main\\java\\first\\common\\util\\file\\testFile\\TEST_FILE_UTF_8";
		//System.out.println(fUtil.readFile(filePath));
		
		File inFile  = new File(inFilePath);
		
		if(inFile.isDirectory())
		{
			fUtil.iterateDirectory(inFile);
		}
		else
		{
			fUtil.convertEncoding(inFile);
		}
	}
}