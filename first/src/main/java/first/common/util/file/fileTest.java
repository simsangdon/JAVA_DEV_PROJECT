package first.common.util.file;

import java.io.File;

public class fileTest {

	public static void main(String[] args) {
		
		fileUtil fUtil = new fileUtil();
		String filePath = "src\\main\\java\\first\\common\\util\\file\\testFile\\testFile1-UTF-8";
		//System.out.println(fUtil.readFile(filePath));
		
		File file = new File(filePath);
		String encoding = "UTF-8";
		
		if(file.isDirectory())
		{
			fUtil.iterateDirectory(file, encoding);
		}
		else
		{
			fUtil.convertEncoding(file, encoding);
		}
	}

}
