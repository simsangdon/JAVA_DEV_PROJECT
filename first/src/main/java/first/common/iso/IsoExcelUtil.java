package first.common.iso;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 공통유틸
 *
 * @title
 * @category programID
 * @cdate 2011. 4. 4.
 * @version
 * @author 박성재
 * @tag
 */
public class IsoExcelUtil
{

    private static final char[]     UPER_CHARS      = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    private static final String     DATA_TIME       = "yyyy-MM-dd HH:mm:ss";
    private static final char[]     DIGITS          = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    private static final String     DELIMITER       = "|";
    private static final int        BufferNum       = 1000;
    private static final BigInteger uperCharsLength = BigInteger.valueOf(UPER_CHARS.length);
    private static final Random     rand            = new Random();

    /**
     * <PRE>
     *  Stream Resource를 닫는다
     *  이는 try catch 블럭에 쓰일수 있지만 여기 서는 수동으로 닫느다.
     * </PRE>
     *
     * @location org.noru.utils.CommonUtil.java
     * @cdate 2014. 11. 3.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param close
     */
    private static final void closeResource(Closeable close)
    {
        if (close != null)
        {
            try
            {
                close.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * <PRE>
     *  컬럼이름을 가지고 colnumber를 만들어 준다.
     *  A -&gt; 0
     *  B -&gt; 1
     *  C -&gt; 2 로 치환된다.
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 28.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param str
     * @return
     */
    private static final int colNameToNum(String str)
    {
        return Integer.parseInt(excelColumnCharsToNumber(str)) - 1;
    }

    /**
     * <PRE>
     * 두바이트가 같은 값인지 검사한다.
     * </PRE>
     *
     * @optitle
     * @cdate 2012. 4. 30.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param src
     * @param target
     * @return
     */
    private static final boolean compareByte(byte src, byte target)
    {
        if (src != target)
        {
            return false;
        } else
        {
            return true;
        }
    }

    /**
     * 바이트배열의 값들이 같은지 검사한다.
     *
     * @optitle
     * @cdate 2011. 3. 27.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param src
     * @param target
     * @return
     */
    private static final boolean compareBytes(byte[] src, byte[] target)
    {
        if (src.length == target.length)
        {
            for (int i = 0; i < target.length; i++)
            {
                if (!compareByte(src[i], target[i]))
                {
                    return false;
                }
            }
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * <PRE>
     *  OS에 맞는 패스로 변경을 해준다
     * </PRE>
     *
     * @location org.noru.utils.FileUtil.java
     * @cdate 2013. 10. 1.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param fileName
     * @return
     */
    private static final String convertFilePathFitOs(String fileName)
    {
        return escapeFileName(fileName).replace("\\", File.separator).replace("/", File.separator);
    }

    /**
     * <PRE>
     * 만일 D:\temp.txt 같은 문자의 파일 명이 들어 왔을 때 인식하지 못하는 문제에 봉착한다.
     * 즉 \t문자를 tab으로 인식해서 File을 열거나 쓸때 에러를 낸다.
     * \b  \t  \n  \f  \r 인 문자를 Escape 해야된다.
     * </PRE>
     *
     * @location org.noru.utils.FileUtil.java
     * @cdate 2013. 10. 14.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param fileName
     * @return
     */
    private static final String escapeFileName(String fileName)
    {
        return fileName.replaceAll("\b", "\\\\b").replaceAll("\t", "\\\\t").replaceAll("\n", "\\\\n").replaceAll("\f", "\\\\f").replaceAll("\r", "\\\\r");
    }

    /**
     * <PRE>
     *  컬럼이름을 가지고 colnumber를 만들어 준다. 가변길이에 대한 유연하게 손을 좀 봐야된다.
     *  A -> 1
     *  B -> 2
     *  C -> 3 로 치환된다.
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 28.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param input
     * @return
     */
    private static final String excelColumnCharsToNumber(String input) throws NumberFormatException
    {
        BigInteger result = BigInteger.ZERO;

        input = input.toUpperCase(); // 대문자로 치환한다. 모든것을

        for (int ctr = 0; ctr < input.length(); ++ctr)
        {
            int position = getPosition(UPER_CHARS, input.charAt(ctr)) + 1;

            if (position > 0)
            {
                result = result.add(uperCharsLength.pow(input.length() - ctr - 1).multiply(BigInteger.valueOf(position)));
            } else
            {
                throw new NumberFormatException(input + " is Not ExcelNumber !!");
            }
        }

        return numberToString(result);
    }

    /**
     * <PRE>
     * 파일의 처음 바이트 부분이 같은지 검사한다. 보통은 같은 형식의 파일이면 앞에 부분이 같다.
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 29.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param fileName
     * @param src
     * @return
     * @throws IOException
     */
    private static final boolean fileHeaderCompare(String fileName, byte[] src)
    {
        byte[] bin = null;
        BufferedInputStream bis = null;
        try
        {
            bis = new BufferedInputStream(new FileInputStream(getFile(fileName)));
            bin = new byte[src.length];
            bis.read(bin);

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        closeResource(bis);

        return compareBytes(src, bin);
    }

    private static final InputStream fileToInputStream(String fileName) throws FileNotFoundException
    {
        File file = getFile(fileName);
        if (file.exists())
        {
            return new FileInputStream(file);
        } else
        {
            return null;
        }
    }

    private static final OutputStream fileToOutputStream(String fileName) throws FileNotFoundException
    {
        return new FileOutputStream(getFile(fileName));
    }

    private static final Cell getCell(Row row, int cellNum)
    {
        return getCell(row, cellNum, Cell.CELL_TYPE_STRING);
    }

    private static final Cell getCell(Row row, int cellNum, int cellType)
    {
        Cell recell = null;
        if (row.getCell(cellNum) != null)
        {
            recell = row.getCell(cellNum);
        } else
        {
            recell = row.createCell(cellNum, cellType);
        }
        return recell;
    }

    private static final Cell getCell(Row row, String cellName)
    {
        return getCell(row, colNameToNum(cellName.toUpperCase()));
    }

    private static final String getCellString(Cell cell)
    {
        // Bug Fix 20131117
        if (cell == null)
        {
            return "";
        }
        try
        {
            return objectToString(getCellValue(cell));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * <PRE>
     *  셀을 인수로 받아서 셀값을 얻어 온다.
     * </PRE>
     *
     * @optitle
     * @cdate 2012. 3. 16.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param cell
     * @return
     */
    private static final Object getCellValue(Cell cell)
    {
        Object value = null;
        // Bug Fix 20131117
        if (cell == null)
        {
            return value;
        }
        try
        {
            switch (cell.getCellType())
            {
            case Cell.CELL_TYPE_STRING:
            {
                value = cell.getRichStringCellValue().getString();
                break;
            }
            case Cell.CELL_TYPE_NUMERIC:
            {
                if (HSSFDateUtil.isCellDateFormatted(cell))
                {
                    SimpleDateFormat formatter = new SimpleDateFormat(DATA_TIME);
                    value = formatter.format(cell.getDateCellValue());
                } else
                {
                    value = new Double(cell.getNumericCellValue()).longValue(); // double 형태 2E+01 이런 형태를 20으로 변경해 준다.
                }
                break;
            }
            case Cell.CELL_TYPE_BOOLEAN:
            {
                value = cell.getBooleanCellValue();
                break;
            }
            case Cell.CELL_TYPE_FORMULA: // 값이 수식일때
            {
                // value = cell.getCellFormula() + ""; //수식자체를 얻어 올때
                switch (cell.getCachedFormulaResultType())
                { // Get the type of Formula 수식이아닌 값을 얻어 올때
                case Cell.CELL_TYPE_STRING:
                {
                    value = cell.getStringCellValue();
                    break;
                }
                case Cell.CELL_TYPE_NUMERIC:
                {
                    value = new Double(cell.getNumericCellValue()).longValue(); // double 형태 2E+01 이런 형태를 20으로 변경해 준다.
                    break;
                }
                case Cell.CELL_TYPE_BOOLEAN:
                {
                    value = cell.getBooleanCellValue();
                    break;
                }
                default:
                {
                    value = cell.getStringCellValue();
                    break;
                }
                }
                break;
            }
            default:
            {
                value = cell.getStringCellValue();
                break;
            }
            }
        } catch (Exception e)
        {
            System.out.println("Row:" + cell.getRow().getRowNum() + " Cell:" + cell.getColumnIndex() + " Error:" + e.getMessage());
        }
        return value;
    }

    private static final File getFile(String fileName)
    {
        return new File(escapeFileName(fileName));
    }

    /**
     * 파일의 패스를 반환한다. "D:/noru/temp\\temp.txt.org \\와 / 의path가 썪여서 들어 올수도 있다.
     *
     * @optitle
     * @cdate 2011. 8. 1.
     * @version 1.0
     * @author Administrator
     * @tag
     * @param fileName
     * @return
     */
    private static final String getFilePath(String fileName)
    {
        String rtnStr = "";

        if (isEmpty(fileName))
        {
            return rtnStr;
        }

        fileName = escapeFileName(fileName);

        boolean isNotWin = fileName.lastIndexOf("/") > fileName.lastIndexOf("\\");

        if (fileName.lastIndexOf("/") > 0 && isNotWin)
        {
            rtnStr = fileName.substring(0, fileName.lastIndexOf("/") + 1);
        } else if (fileName.lastIndexOf("\\") > 0)
        {
            rtnStr = fileName.substring(0, fileName.lastIndexOf("\\") + 1);
        }
        return convertFilePathFitOs(rtnStr);
    }

    private static final int getPosition(char[] args, char chk)
    {
        int position = 0;
        if (args != null)
        {
            for (char comp : args)
            {
                if (comp == chk)
                {
                    return position;
                }
                position++;
            }
        }
        return Integer.MIN_VALUE; // 만일 찾는값이 없을때는 에러를 내게 만든다
    }

    private static final String getRandomString(int length)
    {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++)
        {
            buffer.append(DIGITS[rand.nextInt(DIGITS.length)]);
        }
        return buffer.toString();
    }

    private static final Row getRow(Sheet sheet, int rowNum)
    {
        if (sheet.getRow(rowNum) != null)
        {
            return sheet.getRow(rowNum);
        } else
        {
            return sheet.createRow(rowNum);
        }
    }

    private static final Sheet getSheet(String fileName, int sheetNum) throws IOException
    {
        return getWorkBook(fileName).getSheetAt(sheetNum);
    }

    private static final Sheet getSheet(Workbook wb, int sheetNum)
    {
        try
        {
            return wb.getSheetAt(sheetNum);
        } catch (Exception e)
        {
            return wb.createSheet();
        }
    }

    /**
     * <PRE>
     * 쉬트리스트를 얻는다.
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 9. 12.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param wb
     * @return
     */
    private static final List<Sheet> getSheets(Workbook wb)
    {
        if (wb == null || wb.getNumberOfSheets() == 0)
        {
            return null;
        }
        List<Sheet> result = new ArrayList<Sheet>();
        for (int i = 0; i < wb.getNumberOfSheets(); i++)
        {
            result.add(wb.getSheetAt(i));
        }
        return result;
    }

    /**
     * <PRE>
     * 대용량 쓰기가 가능한 엑셀을 얻는다.
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 9. 10.
     * @version 1.0
     * @author 박성재
     * @tag
     * @return
     */
    private static final Workbook getSXSSFWorkbook()
    {
        return new SXSSFWorkbook(BufferNum);
    }

    /**
     * <PRE>
     * 쓰기용도의 workBook은 대용량이 가능한것으로 얻는다.
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 8. 30.
     * @version 1.0
     * @author 박성재
     * @tag
     * @return
     */
    private static final Workbook getWorkBook()
    {
        return getSXSSFWorkbook();
    }

    /**
     * <PRE>
     * 기존파일에서 읽어 드리는 것은 아래와 같이 한다.
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 8. 30.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param fileName
     * @return
     * @throws IOException
     */
    private static final Workbook getWorkBook(String fileName) throws IOException
    {
        if (IsoExcelUtil.isXSSF(fileName))
        { // Office 2007 파일
            return new XSSFWorkbook(fileToInputStream(fileName));
        } else if (IsoExcelUtil.isHSSF(fileName))
        { // Office 2007 이전파일
            POIFSFileSystem fs = new POIFSFileSystem(fileToInputStream(fileName));
            return new HSSFWorkbook(fs);
        } else
        { // 형식을 만족하지 못하면 Exception을 발생시킨다.
            return null;
        }
    }

    /**
     * 입력한 값이 null 또는 null String 일 경우 true를 return 한다.
     *
     * <pre>
     *
     * [사용 예제]
     *
     * isEmpty("")      ===> true
     * isEmpty(null)    ===> true
     * isEmpty("1")     ===> false
     *
     * </pre>
     *
     * @param value
     * @return boolean
     */
    private static final boolean isEmpty(String value)
    {
        if (value == null || value.isEmpty())
        {
            return true;
        }
        return false;
    }

    /**
     * <PRE>
     * office 97 - 2007의 파일인지를 구분한다.
     * 보통은 xls확장자 바이트값을 얻을때는 아래와 같이 해주던가 아래와 같이 캐스팅한다.
     * Integer.decode(&quot;0xE0&quot;).byteValue(), (byte)0xCF
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 3. 27.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param filename
     * @return
     * @throws IOException
     */
    private static final boolean isHSSF(String filename) throws IOException
    {
        return fileHeaderCompare(filename, new byte[] { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0 });
    }

    /**
     * <PRE>
     * 2007 xlsx 확장자를 가지는 파일포맷 여부
     * System.out.format(&quot;%02X &quot;, bin[i]);
     * %s    : 문자열
     * %c    : 문자 1개
     * %d    : +-부호 있는 정수
     * %u    : +-부호 없는 정수
     * %f    : 실수
     * %0.3f : 실수 (소수점 3자리까지 나오게)
     * %X    : 16진수 대문자로
     * %x    : 16진수 소문자로
     * %02X  : 16진수 대문자로. 2자리 헥사에서, 앞의 빈 칸 있으면 0으로 채움
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 3. 27.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param filename
     * @return
     * @throws IOException
     */
    private static final boolean isXSSF(String filename) throws IOException
    {
        return fileHeaderCompare(filename, new byte[] { 0x50, 0x4b, 0x03, 0x04 });
    }

    private static final String join(double... input)
    {
        return join(DELIMITER, input);
    }

    private static final String join(float... input)
    {
        return join(DELIMITER, input);
    }

    private static final String join(int... input)
    {
        return join(DELIMITER, input);
    }

    private static final String join(long... input)
    {
        return join(DELIMITER, input);
    }

    private static final String join(Object... input)
    {
        return join(DELIMITER, input);
    }

    private static final String join(short... input)
    {
        return join(DELIMITER, input);
    }

    private static final String join(String delimiter, double... input)
    {
        if (input == null)
        {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < input.length; i++)
        {
            buffer.append(input[i]);
            if (i != input.length - 1)
            {
                buffer.append(delimiter); // 마지막엔 안붙인다.
            }
        }
        return buffer.toString();
    }

    private static final String join(String delimiter, float... input)
    {
        if (input == null)
        {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < input.length; i++)
        {
            buffer.append(input[i]);
            if (i != input.length - 1)
            {
                buffer.append(delimiter);// 마지막엔 안붙인다.
            }
        }
        return buffer.toString();
    }

    private static final String join(String delimiter, int... input)
    {
        if (input == null)
        {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < input.length; i++)
        {
            buffer.append(input[i]);
            if (i != input.length - 1)
            {
                buffer.append(delimiter);// 마지막엔 안붙인다.
            }
        }
        return buffer.toString();
    }

    private static final String join(String delimiter, long... input)
    {
        if (input == null)
        {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < input.length; i++)
        {
            buffer.append(input[i]);
            if (i != input.length - 1)
            {
                buffer.append(delimiter);// 마지막엔 안붙인다.
            }
        }
        return buffer.toString();
    }

    private static final String join(String delimiter, Object... input)
    {
        if (input == null)
        {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < input.length; i++)
        {
            buffer.append(input[i]);
            if (i != input.length - 1)
            { // 마지막엔 안붙인다.
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

    private static final String join(String delimiter, short... input)
    {
        if (input == null)
        {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < input.length; i++)
        {
            buffer.append(input[i]);
            if (i != input.length - 1)
            {
                buffer.append(delimiter); // 마지막엔 안붙인다.
            }
        }
        return buffer.toString();
    }

    private static final void makeDir(String dirName) throws IOException
    {
        File dir = getFile(dirName);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
    }

    /**
     * <PRE>
     *  숫자를 엑셀컬럼 이름으로 변화시킨다.
     *  java 에선 0을 첫번째로 인식하므로 그에 대한 처리를 위한메소드
     *  0을 A 로 변환 1를 B로 변환
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 28.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param i
     * @return
     */
    private static final String numberToChars(int i)
    {
        return numberToExcelColumnChars(i + 1);
    }

    /**
     * <PRE>
     *  무한히 큰수도 처리 할 수 있게 변경함
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @Method numberToExcelColumnChars
     * @cdate 2015. 9. 16. 오후 3:05:58
     * @version 1.0
     * @author Administrator
     * @param input
     * @return
     */
    private static final String numberToExcelColumnChars(BigInteger input)
    {
        String rtnStr = "";

        BigInteger time = BigInteger.ZERO; // 목
        int rest = 0; // 나머지

        while (input.compareTo(uperCharsLength) == 1) // input > UPER_CHARS.length
        { // 26보다 크면

            time = input.divide(uperCharsLength); // 목
            rest = input.remainder(uperCharsLength).intValue(); // 나머지

            // CommonUtil.print(time, rest);
            if (time.compareTo(uperCharsLength.add(BigInteger.ONE)) > -1)
            { // 목이 27보다 크면
                if (rest == 0)
                { // 나머지가 없으면 나머지는 Z로 치환해준다.
                    rtnStr = UPER_CHARS[UPER_CHARS.length - 1] + rtnStr; // 나머지 넣어주고
                    input = time.subtract(BigInteger.ONE); // Z로 치환한 것을 뺀 목을 넘겨 준다.
                } else
                {
                    rtnStr = UPER_CHARS[rest - 1] + rtnStr; // 나머지만 넣어 주고 다음에 계산
                    input = time; // 목을 넘겨 준다.
                }
            } else
            { // 26 이하
                if (rest == 0)
                { // 나머지가 없으면 나머지는 Z로 치환해준다.
                    rtnStr = UPER_CHARS[UPER_CHARS.length - 1] + rtnStr; // 나머지 넣어주고
                    rtnStr = UPER_CHARS[time.subtract(BigInteger.valueOf(2)).intValue()] + rtnStr; // Z로 치환한 값은 빼주고 목도그냥 처리 해줌
                } else
                {
                    rtnStr = UPER_CHARS[rest - 1] + rtnStr; // 나머지 넣어주고
                    rtnStr = UPER_CHARS[time.subtract(BigInteger.ONE).intValue()] + rtnStr; // 목도그냥 처리 해줌
                }

                input = BigInteger.ZERO;
            }
        }

        if (input.compareTo(BigInteger.ZERO) == 1)
        { // 0일땐 모두 처리 된것이므로 해주지 않는다.
            rtnStr += UPER_CHARS[input.subtract(BigInteger.ONE).intValue()]; // 26보다같거나 작은것을 처리 해준다.
        }

        return rtnStr;
    }

    /**
     * <PRE>
     *  숫자를 엑셀컬럼 이름으로 변화시킨다.
     *  1을 A 로 변환 2를 B로 변환
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 28.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param input
     * @return
     */
    private static final String numberToExcelColumnChars(Number input)
    {
        return numberToExcelColumnChars(new BigInteger(numberToString(input)));
    }

    private static final String numberToString(Number input)
    {
        NumberFormat numFmt = NumberFormat.getInstance();
        numFmt.setGroupingUsed(false);
        return numFmt.format(input);
    }

    private static final String objectToString(Object input)
    {
        if (input == null)
        {
            return "";
        } else if (input instanceof CharSequence)
        {
            return input.toString();
        } else if (input instanceof Character)
        {
            return input.toString();
        } else if (input instanceof Number)
        {
            return input + "";
        } else if (input instanceof Boolean)
        {
            return input + "";
        } else if (input instanceof int[])
        {
            return join((int[]) input);
        } else if (input instanceof float[])
        {
            return join((float[]) input);
        } else if (input instanceof long[])
        {
            return join((long[]) input);
        } else if (input instanceof double[])
        {
            return join((double[]) input);
        } else if (input instanceof short[])
        {
            return join((short[]) input);
        } else if (input instanceof CharSequence[])
        {
            return join((Object[]) input);
        } else if (input instanceof Number[])
        {
            return join((Object[]) input);
        } else if (input instanceof Character[])
        {
            return join((Object[]) input);
        } else if (input instanceof Boolean[])
        {
            return join((Object[]) input);
        } else if (input instanceof byte[])
        {
            return new String((byte[]) input);
        } else
        {
            return input.toString();
        }
    }

    private static final void removeRow(Sheet sheet, int rowIndex)
    {
        rowIndex = rowIndex + 1;
        int lastRowNum = sheet.getLastRowNum();
        if (rowIndex >= 0 && rowIndex < lastRowNum)
        {
            sheet.shiftRows(rowIndex, lastRowNum, -1, true, true);
        }
        if (rowIndex == lastRowNum)
        {
            Row removingRow = sheet.getRow(rowIndex);
            if (removingRow != null)
            {
                sheet.removeRow(removingRow);
            }
        }
    }

    private static final void setCell(Row row, int cellNum, Object value)
    {
        setCell(row, cellNum, value, Cell.CELL_TYPE_STRING);
    }

    /**
     * <PRE>
     * 0 -> General
     * 1 -> 0
     * 2 -> 0.00
     * 3 -> #,##0
     * 4 -> #,##0.00
     * 5 -> "$"#,##0_);("$"#,##0)
     * 6 -> "$"#,##0_);[Red]("$"#,##0)
     * 7 -> "$"#,##0.00_);("$"#,##0.00)
     * 8 -> "$"#,##0.00_);[Red]("$"#,##0.00)
     * 9 -> 0%
     * 10 -> 0.00%
     * 11 -> 0.00E+00
     * 12 -> # ?/?
     * 13 -> # ??/??
     * 14 -> m/d/yy
     * 15 -> d-mmm-yy
     * 16 -> d-mmm
     * 17 -> mmm-yy
     * 18 -> h:mm AM/PM
     * 19 -> h:mm:ss AM/PM
     * 20 -> h:mm
     * 21 -> h:mm:ss
     * 22 -> m/d/yy h:mm
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2014. 7. 29.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param row
     * @param cellNum
     * @param value
     * @param cellType
     */
    private static final void setCell(Row row, int cellNum, Object value, int cellType)
    {
        Cell cell = getCell(row, cellNum, cellType);
        if (Cell.CELL_TYPE_NUMERIC == cellType)
        {
            try
            {
                short stype = 1;
                if (objectToString(value).indexOf(".") > -1)
                {
                    stype = 2;
                }
                CellStyle style = row.getSheet().getWorkbook().createCellStyle();
                style.setDataFormat(HSSFDataFormat.getBuiltinFormat(HSSFDataFormat.getBuiltinFormat(stype)));
                Double cellVal = Double.parseDouble(objectToString(value));
                cell.setCellStyle(style);
                cell.setCellValue(cellVal);
            } catch (NumberFormatException e)
            {
                cell.setCellValue(objectToString(value));
            }
        } else
        {
            cell.setCellValue(objectToString(value));
        }
    }

    private static final void writeExcel(Workbook wb, String fileName) throws IOException
    {
        makeDir(getFilePath(fileName)); // 없는 디렉토리는 만들어 준다.
        OutputStream outStream = fileToOutputStream(fileName);
        wb.write(outStream);
        closeResource(outStream);
    }

    public static final void copyRowStyle(Row src, Row tar)
    {
        tar.setHeight(src.getHeight()); // 높이복사
        Cell srcCell = null;
        Cell tarCell = null;
        for (Iterator<Cell> cit = src.cellIterator(); cit.hasNext();)
        {
            srcCell = cit.next();
            tarCell = tar.createCell(srcCell.getColumnIndex());
            tarCell.setCellStyle(srcCell.getCellStyle());
            tarCell.setCellType(srcCell.getCellType());
        }
    }

    /**
     * <PRE>
     *  ROW와 셀 위치를 받아서 셀값을 얻어 온다.
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 5. 30.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param row
     * @param cellNum
     * @return
     */
    public static final String getCellValue(Row row, int cellNum)
    {
        return getCellString(getCell(row, cellNum));
    }

    public static final String getCellValue(Row row, String cellName)
    {
        return getCellString(getCell(row, cellName));
    }

    public static final Workbook getHSSFWorkBook()
    {
        return new HSSFWorkbook();
    }

    public static final Row getRow(Sheet sheet)
    {
        return getRow(sheet, sheet.getLastRowNum() + 1);
    }

    public static final Row getRow(String fileName, int sheetNum, int rowNum) throws IOException
    {
        return getSheet(fileName, sheetNum).getRow(rowNum);
    }

    public static final Row getRow(Workbook wb, int sheetNum, int rowNum)
    {
        return wb.getSheetAt(sheetNum).getRow(rowNum);
    }

    /**
     * <PRE>
     * 파일명을 가지고 쉬트 리스트를 얻는다.
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 9. 12.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param fileName
     * @return
     */
    public static final List<Sheet> getSheets(String fileName)
    {
        Workbook wb = null;
        try
        {
            wb = getWorkBook(fileName);
        } catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        return getSheets(wb);
    }

    public static final Workbook getXSSFWorkbook()
    {
        return new XSSFWorkbook();
    }

    public static final boolean haveValue(Sheet sheet, int columnNo, String compVal)
    {
        for (Iterator<Row> rit = sheet.rowIterator(); rit.hasNext();)
        {
            if ((getCellValue(rit.next().getCell(columnNo)) + "").trim().indexOf(compVal) > -1)
            { // 해당셀을 얻어 와서 비교값하고 비교
                return true;
            }
        }
        return false;
    }

    public static final boolean isExcelFile(String fileName)
    {
        try
        {
            if (isXSSF(fileName) || isHSSF(fileName))
            {
                return true;
            } else
            {
                return false;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * <PRE>
     * 테스트용 Excel 파일을 만든다.
     * EX) ExcelUtil.makeTestData(7, 3, fileName);
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 9. 3.
     * @version 1.0
     * @author 박성재
     * @tag
     * @param rowNum
     * @param colNum
     * @param fileName
     */
    public static final void makeTestDataFile(int rowNum, int colNum, String fileName)
    {
        Workbook wb = getWorkBook();
        Sheet sheet = getSheet(wb, 0);
        // 인수만큼의 row와 column 만큼 데이터를 만든다.
        for (int i = 0; i < rowNum; i++)
        {
            Row row = getRow(sheet, i);
            for (int j = 0; j < colNum; j++)
            {
                Cell cell = getCell(row, j);
                cell.setCellValue(numberToChars(j) + (i + 1) + " : " + getRandomString(10));
            }
        }

        try
        {
            writeExcel(wb, fileName);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static final void moveToLastRow(Sheet sheet, int rowIndex)
    {
        sheet.shiftRows(rowIndex, rowIndex, sheet.getLastRowNum() - rowIndex + 1, true, true);
        removeRow(sheet, rowIndex);
    }

    public static final String numberToExcelColumnChars(String input)
    {
        return numberToExcelColumnChars(new BigInteger(input));
    }

    public static final String numToChars(int input)
    {
        return numberToExcelColumnChars(input);
    }

    public static final Map<String, Object> rowToMap(Row row)
    {
        // Map<String, Object> rtnMap = new LinkedHashMap<String, Object>();
        Map<String, Object> rtnMap = new LinkedHashMap<String, Object>();
        for (int i = 0; i < row.getLastCellNum(); i++)
        {
            if (row.getCell(i) != null)
            {
                Cell cell = row.getCell(i);
                rtnMap.put(IsoExcelUtil.numberToChars(i), IsoExcelUtil.getCellValue(cell));
            } else
            {
                rtnMap.put(IsoExcelUtil.numberToChars(i), "");
            }
        }
        return rtnMap;
    }

    public static final void setAddCell(Row row, int cellNum, Object value)
    {
        Cell cell = getCell(row, cellNum);
        cell.setCellValue(getCellValue(cell) + "," + value);
    }

    public static final void setAddCell(Row row, int cellNum, Object value, String sepr)
    {
        Cell cell = getCell(row, cellNum);
        cell.setCellValue(getCellValue(cell) + sepr + value);
    }

    public static final void setCell(Row row, String cellName, Object value)
    {
        setCell(row, colNameToNum(cellName.toUpperCase()), objectToString(value));
    }

    public static final void voidRow(Sheet sheet, int rowIndex)
    {
        Row row = sheet.getRow(rowIndex);
        Cell cell = null;
        for (Iterator<Cell> cit = row.cellIterator(); cit.hasNext();)
        {
            cell = cit.next();
            cell.setCellValue("");
            cell.setCellStyle(sheet.getWorkbook().createCellStyle());
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }
    }

    public static void main(String[] args) throws IOException
    {
        Workbook wb = getWorkBook("C:\\Users\\user\\javaStudy\\temp.xlsx");
        Sheet sheet = getSheet(wb, 0); // 첫번째 쉬트를 연다
        Row row = getRow(sheet, 3); // 네번째 row을 연다.
        Cell cell = getCell(row, 5); // 6번째 셀을 가져온다.

        System.out.println(getCellValue(cell));

        setCell(row, 5, "노루만세"); // 6번째 셀 F에 값을 쓴다
        /*로그인 계정이 Admin계정이 아니면 권한이 없을수 있으므로 내문서 폴더 밑으로 파일위치를 지정 한다.*/
        writeExcel(wb, "C:\\Users\\user\\javaStudy\\temp_work.xlsx"); // 변경된 내용을 다른 파일을 쓴다.
    }
}