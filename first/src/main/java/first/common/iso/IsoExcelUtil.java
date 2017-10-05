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
 * ������ƿ
 *
 * @title
 * @category programID
 * @cdate 2011. 4. 4.
 * @version
 * @author �ڼ���
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
     *  Stream Resource�� �ݴ´�
     *  �̴� try catch ���� ���ϼ� ������ ���� ���� �������� �ݴ���.
     * </PRE>
     *
     * @location org.noru.utils.CommonUtil.java
     * @cdate 2014. 11. 3.
     * @version 1.0
     * @author �ڼ���
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
     *  �÷��̸��� ������ colnumber�� ����� �ش�.
     *  A -&gt; 0
     *  B -&gt; 1
     *  C -&gt; 2 �� ġȯ�ȴ�.
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 28.
     * @version 1.0
     * @author �ڼ���
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
     * �ι���Ʈ�� ���� ������ �˻��Ѵ�.
     * </PRE>
     *
     * @optitle
     * @cdate 2012. 4. 30.
     * @version 1.0
     * @author �ڼ���
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
     * ����Ʈ�迭�� ������ ������ �˻��Ѵ�.
     *
     * @optitle
     * @cdate 2011. 3. 27.
     * @version 1.0
     * @author �ڼ���
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
     *  OS�� �´� �н��� ������ ���ش�
     * </PRE>
     *
     * @location org.noru.utils.FileUtil.java
     * @cdate 2013. 10. 1.
     * @version 1.0
     * @author �ڼ���
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
     * ���� D:\temp.txt ���� ������ ���� ���� ��� ���� �� �ν����� ���ϴ� ������ �����Ѵ�.
     * �� \t���ڸ� tab���� �ν��ؼ� File�� ���ų� ���� ������ ����.
     * \b  \t  \n  \f  \r �� ���ڸ� Escape �ؾߵȴ�.
     * </PRE>
     *
     * @location org.noru.utils.FileUtil.java
     * @cdate 2013. 10. 14.
     * @version 1.0
     * @author �ڼ���
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
     *  �÷��̸��� ������ colnumber�� ����� �ش�. �������̿� ���� �����ϰ� ���� �� ���ߵȴ�.
     *  A -> 1
     *  B -> 2
     *  C -> 3 �� ġȯ�ȴ�.
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 28.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param input
     * @return
     */
    private static final String excelColumnCharsToNumber(String input) throws NumberFormatException
    {
        BigInteger result = BigInteger.ZERO;

        input = input.toUpperCase(); // �빮�ڷ� ġȯ�Ѵ�. ������

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
     * ������ ó�� ����Ʈ �κ��� ������ �˻��Ѵ�. ������ ���� ������ �����̸� �տ� �κ��� ����.
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 29.
     * @version 1.0
     * @author �ڼ���
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
     *  ���� �μ��� �޾Ƽ� ������ ��� �´�.
     * </PRE>
     *
     * @optitle
     * @cdate 2012. 3. 16.
     * @version 1.0
     * @author �ڼ���
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
                    value = new Double(cell.getNumericCellValue()).longValue(); // double ���� 2E+01 �̷� ���¸� 20���� ������ �ش�.
                }
                break;
            }
            case Cell.CELL_TYPE_BOOLEAN:
            {
                value = cell.getBooleanCellValue();
                break;
            }
            case Cell.CELL_TYPE_FORMULA: // ���� �����϶�
            {
                // value = cell.getCellFormula() + ""; //������ü�� ��� �ö�
                switch (cell.getCachedFormulaResultType())
                { // Get the type of Formula �����̾ƴ� ���� ��� �ö�
                case Cell.CELL_TYPE_STRING:
                {
                    value = cell.getStringCellValue();
                    break;
                }
                case Cell.CELL_TYPE_NUMERIC:
                {
                    value = new Double(cell.getNumericCellValue()).longValue(); // double ���� 2E+01 �̷� ���¸� 20���� ������ �ش�.
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
     * ������ �н��� ��ȯ�Ѵ�. "D:/noru/temp\\temp.txt.org \\�� / ��path�� ������ ��� �ü��� �ִ�.
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
        return Integer.MIN_VALUE; // ���� ã�°��� �������� ������ ���� �����
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
     * ��Ʈ����Ʈ�� ��´�.
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 9. 12.
     * @version 1.0
     * @author �ڼ���
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
     * ��뷮 ���Ⱑ ������ ������ ��´�.
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 9. 10.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @return
     */
    private static final Workbook getSXSSFWorkbook()
    {
        return new SXSSFWorkbook(BufferNum);
    }

    /**
     * <PRE>
     * ����뵵�� workBook�� ��뷮�� �����Ѱ����� ��´�.
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 8. 30.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @return
     */
    private static final Workbook getWorkBook()
    {
        return getSXSSFWorkbook();
    }

    /**
     * <PRE>
     * �������Ͽ��� �о� �帮�� ���� �Ʒ��� ���� �Ѵ�.
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 8. 30.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param fileName
     * @return
     * @throws IOException
     */
    private static final Workbook getWorkBook(String fileName) throws IOException
    {
        if (IsoExcelUtil.isXSSF(fileName))
        { // Office 2007 ����
            return new XSSFWorkbook(fileToInputStream(fileName));
        } else if (IsoExcelUtil.isHSSF(fileName))
        { // Office 2007 ��������
            POIFSFileSystem fs = new POIFSFileSystem(fileToInputStream(fileName));
            return new HSSFWorkbook(fs);
        } else
        { // ������ �������� ���ϸ� Exception�� �߻���Ų��.
            return null;
        }
    }

    /**
     * �Է��� ���� null �Ǵ� null String �� ��� true�� return �Ѵ�.
     *
     * <pre>
     *
     * [��� ����]
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
     * office 97 - 2007�� ���������� �����Ѵ�.
     * ������ xlsȮ���� ����Ʈ���� �������� �Ʒ��� ���� ���ִ��� �Ʒ��� ���� ĳ�����Ѵ�.
     * Integer.decode(&quot;0xE0&quot;).byteValue(), (byte)0xCF
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 3. 27.
     * @version 1.0
     * @author �ڼ���
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
     * 2007 xlsx Ȯ���ڸ� ������ �������� ����
     * System.out.format(&quot;%02X &quot;, bin[i]);
     * %s    : ���ڿ�
     * %c    : ���� 1��
     * %d    : +-��ȣ �ִ� ����
     * %u    : +-��ȣ ���� ����
     * %f    : �Ǽ�
     * %0.3f : �Ǽ� (�Ҽ��� 3�ڸ����� ������)
     * %X    : 16���� �빮�ڷ�
     * %x    : 16���� �ҹ��ڷ�
     * %02X  : 16���� �빮�ڷ�. 2�ڸ� ��翡��, ���� �� ĭ ������ 0���� ä��
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 3. 27.
     * @version 1.0
     * @author �ڼ���
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
                buffer.append(delimiter); // �������� �Ⱥ��δ�.
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
                buffer.append(delimiter);// �������� �Ⱥ��δ�.
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
                buffer.append(delimiter);// �������� �Ⱥ��δ�.
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
                buffer.append(delimiter);// �������� �Ⱥ��δ�.
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
            { // �������� �Ⱥ��δ�.
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
                buffer.append(delimiter); // �������� �Ⱥ��δ�.
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
     *  ���ڸ� �����÷� �̸����� ��ȭ��Ų��.
     *  java ���� 0�� ù��°�� �ν��ϹǷ� �׿� ���� ó���� ���Ѹ޼ҵ�
     *  0�� A �� ��ȯ 1�� B�� ��ȯ
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 28.
     * @version 1.0
     * @author �ڼ���
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
     *  ������ ū���� ó�� �� �� �ְ� ������
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @Method numberToExcelColumnChars
     * @cdate 2015. 9. 16. ���� 3:05:58
     * @version 1.0
     * @author Administrator
     * @param input
     * @return
     */
    private static final String numberToExcelColumnChars(BigInteger input)
    {
        String rtnStr = "";

        BigInteger time = BigInteger.ZERO; // ��
        int rest = 0; // ������

        while (input.compareTo(uperCharsLength) == 1) // input > UPER_CHARS.length
        { // 26���� ũ��

            time = input.divide(uperCharsLength); // ��
            rest = input.remainder(uperCharsLength).intValue(); // ������

            // CommonUtil.print(time, rest);
            if (time.compareTo(uperCharsLength.add(BigInteger.ONE)) > -1)
            { // ���� 27���� ũ��
                if (rest == 0)
                { // �������� ������ �������� Z�� ġȯ���ش�.
                    rtnStr = UPER_CHARS[UPER_CHARS.length - 1] + rtnStr; // ������ �־��ְ�
                    input = time.subtract(BigInteger.ONE); // Z�� ġȯ�� ���� �� ���� �Ѱ� �ش�.
                } else
                {
                    rtnStr = UPER_CHARS[rest - 1] + rtnStr; // �������� �־� �ְ� ������ ���
                    input = time; // ���� �Ѱ� �ش�.
                }
            } else
            { // 26 ����
                if (rest == 0)
                { // �������� ������ �������� Z�� ġȯ���ش�.
                    rtnStr = UPER_CHARS[UPER_CHARS.length - 1] + rtnStr; // ������ �־��ְ�
                    rtnStr = UPER_CHARS[time.subtract(BigInteger.valueOf(2)).intValue()] + rtnStr; // Z�� ġȯ�� ���� ���ְ� �񵵱׳� ó�� ����
                } else
                {
                    rtnStr = UPER_CHARS[rest - 1] + rtnStr; // ������ �־��ְ�
                    rtnStr = UPER_CHARS[time.subtract(BigInteger.ONE).intValue()] + rtnStr; // �񵵱׳� ó�� ����
                }

                input = BigInteger.ZERO;
            }
        }

        if (input.compareTo(BigInteger.ZERO) == 1)
        { // 0�϶� ��� ó�� �Ȱ��̹Ƿ� ������ �ʴ´�.
            rtnStr += UPER_CHARS[input.subtract(BigInteger.ONE).intValue()]; // 26���ٰ��ų� �������� ó�� ���ش�.
        }

        return rtnStr;
    }

    /**
     * <PRE>
     *  ���ڸ� �����÷� �̸����� ��ȭ��Ų��.
     *  1�� A �� ��ȯ 2�� B�� ��ȯ
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 28.
     * @version 1.0
     * @author �ڼ���
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
     * @author �ڼ���
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
        makeDir(getFilePath(fileName)); // ���� ���丮�� ����� �ش�.
        OutputStream outStream = fileToOutputStream(fileName);
        wb.write(outStream);
        closeResource(outStream);
    }

    public static final void copyRowStyle(Row src, Row tar)
    {
        tar.setHeight(src.getHeight()); // ���̺���
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
     *  ROW�� �� ��ġ�� �޾Ƽ� ������ ��� �´�.
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 5. 30.
     * @version 1.0
     * @author �ڼ���
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
     * ���ϸ��� ������ ��Ʈ ����Ʈ�� ��´�.
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 9. 12.
     * @version 1.0
     * @author �ڼ���
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
            { // �ش缿�� ��� �ͼ� �񱳰��ϰ� ��
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
     * �׽�Ʈ�� Excel ������ �����.
     * EX) ExcelUtil.makeTestData(7, 3, fileName);
     * </PRE>
     *
     * @location org.noru.utils.ExcelUtil.java
     * @cdate 2013. 9. 3.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param rowNum
     * @param colNum
     * @param fileName
     */
    public static final void makeTestDataFile(int rowNum, int colNum, String fileName)
    {
        Workbook wb = getWorkBook();
        Sheet sheet = getSheet(wb, 0);
        // �μ���ŭ�� row�� column ��ŭ �����͸� �����.
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
        Sheet sheet = getSheet(wb, 0); // ù��° ��Ʈ�� ����
        Row row = getRow(sheet, 3); // �׹�° row�� ����.
        Cell cell = getCell(row, 5); // 6��° ���� �����´�.

        System.out.println(getCellValue(cell));

        setCell(row, 5, "��縸��"); // 6��° �� F�� ���� ����
        /*�α��� ������ Admin������ �ƴϸ� ������ ������ �����Ƿ� ������ ���� ������ ������ġ�� ���� �Ѵ�.*/
        writeExcel(wb, "C:\\Users\\user\\javaStudy\\temp_work.xlsx"); // ����� ������ �ٸ� ������ ����.
    }
}