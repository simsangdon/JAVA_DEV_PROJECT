package first.common.iso;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class IsoFileStringUtil
{
    public static final String[]   TABLE_STRING           = { "��", "��", "��", "��", "��", "��", "��", "��", "��", "��", "-"};
    public static final String     TOP_LEFT               = TABLE_STRING[0];
    public static final String     TOP_MIDLE              = TABLE_STRING[1];
    public static final String     TOP_RIGHT              = TABLE_STRING[2];
    public static final String     VERTICAL               = TABLE_STRING[3];
    public static final String     MIDDLE_LEFT            = TABLE_STRING[4];
    public static final String     MIDDLE_RIGHT           = TABLE_STRING[5];
    public static final String     CENTER                 = TABLE_STRING[6];
    public static final String     BUTTOM_LEFT            = TABLE_STRING[7];
    public static final String     BUTTOM_MIDDLE          = TABLE_STRING[8];
    public static final String     BUTTOM_RIGHT           = TABLE_STRING[9];
    public static final String     DASH                   = TABLE_STRING[10];
    public static int              LOG_STRING_SIZE        = 80;
    public static final String     NEW_LINE               = "\r\n";
    public static final String     REX_2BYTE_CHAR         = "[\u1100-\uFFE6]";

    /**
     * ����ã�⿡�� Filter �� ���� �̸��� ��´�.
     *
     * @title
     * @category programID
     * @cdate 2013. 8. 21.
     * @version
     * @author Administrator
     * @tag
     */
    public class FileStartWithFilter implements FilenameFilter
    {
        private final String fileName;

        public FileStartWithFilter(String fileName)
        {
            this.fileName = fileName;
        }

        @Override
        public boolean accept(File dir, String name)
        {
            if (name != null)
            {
                return this.fileName.startsWith(name);
            }
            return false;
        }
    }

    private static final FileSystem FILE_SYSTEMS     = FileSystems.getDefault();
    private static final int        BUFFER_SIZE      = 1024 * 8;
    private static final String     SPACE            = " ";
    private static final String     UTF8_BOM         = "\uFEFF";
    private static final String     REX_TAB          = "\t";
    private static final String     TAB_SPACE        = "    ";
    private static final String     REX_NEW_LINE     = "\r\n|\n\r|\r|\n";
    private static final String     REX_JAVA_COMMENT = "//[^\r\n]*";
    private static final String     DELIMITER        = "|";
    private static final String     REX_SQL_COMMENT  = "--[^\r\n]*";
    private static final String     LEFT_WAP         = "{ ";
    private static final String     RIGHT_WAP        = " } ";
    private static final String     PRINT_YN         = "PRINT_YN";

    private static final String addSpace(String input, int times, String added)
    {
        StringBuilder sb = new StringBuilder();
        String tabs = "";
        for (int i = 0; i < times; i++)
        {
            tabs += " ";
        }
        tabs += added;
        String[] result = toStringArray(input);
        for (int i = 0; i < result.length; i++)
        {
            if (i == result.length - 1)
            {
                sb.append(tabs).append(result[i]);
            } else
            {
                sb.append(tabs).append(result[i]).append(NEW_LINE);
            }
        }
        return sb.toString();
    }

    private static final String addTabs(String s, int times)
    {
        return addTabs(s, times, "");
    }

    private static final String addTabs(String input, int times, String added)
    {
        return addSpace(input, times * 4, added);
    }

    /**
     * �ι迭�� ��ģ��. source �� 2��° ÷�ں��� 4���� target �� 3��° ÷�ں��� ���� System.arraycopy( source, 2, target, 3, 4 );
     *
     * @optitle
     * @cdate 2010. 6. 3.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param <T>
     * @param a
     * @param b
     * @return
     */
    private static final <T> T[] arrayConcat(T[] a, T[] b)
    {
        final int alen = a.length;
        final int blen = b.length;
        if (alen == 0)
        {
            return b;
        }
        if (blen == 0)
        {
            return a;
        }
        final T[] result = (T[]) Array.newInstance(a.getClass().getComponentType(), alen + blen);
        System.arraycopy(a, 0, result, 0, alen);
        System.arraycopy(b, 0, result, alen, blen);
        return result;
    }

    /**
     * �迭�� ���ϴ� ��ġ�� ���� �ϳ� �� �ִ´�. source �� 2��° ÷�ں��� 4���� target �� 3��° ÷�ں��� ���� System.arraycopy( source, 2, target, 3, 4 );
     *
     * @optitle
     * @cdate 2010. 6. 3.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param <T>
     * @param source
     * @param added
     * @param position
     * @return
     */
    private static final <T> T[] arrayInsert(T[] source, T added, int position)
    {
        final int sourceLength = source.length;
        if (position > sourceLength)
        { // ������ ���� �迭���� ũ�� �� �Ʒ� ���� �ִ´�.
            position = sourceLength;
        } else if (position < 0)
        {
            position = 0; // �Ѿ�� ���� ������ ó������ �Ѵ�.
        }
        // �ѱ� ��ü�� �����Ѵ�.
        final T[] target = (T[]) Array.newInstance(source.getClass().getComponentType(), sourceLength + 1);
        System.arraycopy(source, 0, target, 0, position);
        target[position] = added;
        System.arraycopy(source, position, target, position + 1, sourceLength - position);
        return target;
    }

    /**
     * byte�� �޾Ƽ� file�� ����. \b \t \n \f \r �� ���ڸ� Escape �ؾߵȴ�.
     *
     * @optitle
     * @cdate 2011. 5. 27.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param input
     * @param fileName
     * @throws IOException
     */
    private static final boolean bytesToFile(byte[] input, String fileName) throws IOException
    {
        makeDir(getFilePath(fileName));

        FileChannel outChannel = getFileChannel(fileName);

        boolean retVal = false;

        if (outChannel == null)
        {
            return retVal;
        }
        // ���丮�� ������ ������ش�

        retVal = writeBytesToFileChannel(input, outChannel);
        closeResource(outChannel);

        return retVal;
    }

    private static final void closeResource(AutoCloseable close)
    {
        if (close != null)
        {
            try
            {
                close.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static final void closeResource(Closeable close)
    {
        if (close != null)
        {
            try
            {
                close.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
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

    private static final boolean copyFile(File sourceFile, File tarFile)
    {
        // Validate üũ
        if (sourceFile == null || tarFile == null || !sourceFile.exists())
        {
            return false;
        }

        boolean returnVal = true;
        FileInputStream input = null;
        FileOutputStream output = null;
        FileChannel fcin = null;
        FileChannel fcout = null;

        try
        {
            if (!tarFile.getParentFile().exists())
            {
                tarFile.getParentFile().mkdirs();
            }

            if (sourceFile.isDirectory())
            {
                for (String fileName : sourceFile.list())
                {
                    copyFile(new File(sourceFile, fileName), new File(tarFile, fileName));
                }
                return true;
            }

            input = new FileInputStream(sourceFile);
            output = new FileOutputStream(tarFile);

            // ä���� ��� �´�
            fcin = input.getChannel();
            fcout = output.getChannel();

            long size = fcin.size();
            fcin.transferTo(0, size, fcout);

        } catch (IOException e)
        {
            e.printStackTrace();
            returnVal = false;
        }
        // ��� ���ҽ��� �ݴ´�
        closeResource(fcin);
        closeResource(fcout);
        closeResource(output);
        closeResource(input);

        return returnVal;
    }

    private static final boolean delete(String fileName)
    {
        File file = getFile(fileName);
        if (file.exists())
        {
            return file.delete();
        } else
        {
            return false;
        }
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
     * ������ �о OutputStream �� ����
     * </PRE>
     *
     * @location org.noru.utils.FileUtil.java
     * @cdate 2014. 5. 13.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param file
     * @param outputStream
     * @throws IOException
     */
    private static final void fileIntoOutputStream(File file, OutputStream outputStream) throws IOException
    {
        FileInputStream fis = new FileInputStream(file);
        FileChannel fc = fis.getChannel();

        ByteBuffer buf = ByteBuffer.allocateDirect(BUFFER_SIZE); // 8K
        int len = 0;

        while ((len = fc.read(buf)) != -1)
        {
            buf.flip();
            byte[] src = new byte[len];
            /*------------------------------------------------------------------------------------------------------
             * allocateDirect�� ��� Ŀ�δ� ���ۻ����̱⿡
             * ��ü Array�� �������� �żҵ��� get(byte[] src), array()�Լ��� �������� �ʴ´�.
             * �����÷��� get(byte[] src, offset, length)�Լ� ������ �����;ߵǴµ�
             * �̰͵� Ŀ�δ� �����̿��� �ӵ��� �������� ���� �ؾ��� Ȯ���� ������ �����Ѵ�.
             *----------------------------------------------------------------------------------------------------*/
            buf.get(src, 0, len);
            outputStream.write(src, 0, len);
            buf.clear();
        }

        closeResource(fc);
        closeResource(fis);
    }

    private static final byte[] fileToBytes(File file)
    {
        byte[] result = null;
        ByteArrayOutputStream baos = null;
        try
        {
            baos = new ByteArrayOutputStream();

            fileIntoOutputStream(file, baos);

            result = baos.toByteArray();

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        closeResource(baos); // ������ ���ҽ��� �ݴ´�

        return result;
    }

    private static final byte[] fileToBytes(String fileName) throws IOException
    {
        File file = getFile(fileName);
        if (file.exists())
        {
            return fileToBytes(getFile(fileName));
        } else
        {
            throw new IOException("File " + fileName + " is not Found");
        }
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

    /**
     * <PRE>
     *  ������ ��Ʈ��ȭ ��Ų��.
     *  ���������� �󳻿��� �ش�.
     * </PRE>
     *
     * @optitle
     * @cdate 2012. 11. 22.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param fileName
     * @return
     * @throws IOException
     */
    private static final String fileToString(String fileName)
    {
        try
        {
            /*--------------------------------------------------------------------------------------------------
             * UTF-8�� ����� File���Ŀ��� Bom�� ���� ���ش�
             *------------------------------------------------------------------------------------------------*/
            return removeUTF8BOM(new String(fileToBytes(fileName)));
        } catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * <PRE>
     * ���Ͽ� ��� �ִ� ������ Line���� List�� ��´�.
     * </PRE>
     *
     * @optitle
     * @cdate 2012. 3. 30.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param fileName
     * @param limit
     * @return
     * @throws IOException
     */
    private static final String[] fileToStringArray(String fileName, int limit)
    {
        List<String> stringList = new ArrayList<String>();
        File file = getFile(fileName);

        FileReader fr = null;
        BufferedReader br = null;

        try
        {
            // br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"EUC-KR"));
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            int indx = 1;
            while (br.ready())
            {
                String str = br.readLine();
                if (isNotEmpty(str))
                { // ������� �ƴϸ�
                    stringList.add(str);
                    if (indx == limit)
                    {
                        break;
                    }
                    indx++;
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        closeResource(br);
        closeResource(fr);

        return stringList.toArray(new String[0]);
    }

    private static final int getDigit(Number num)
    {
        return (int) Math.log10(num.intValue()) + 1;
    }

    private static final File getFile(String fileName)
    {
        return new File(escapeFileName(fileName));
    }

    /**
     * <PRE>
     *  ������ ä���� ��� �´�.
     * </PRE>
     *
     * @className:org.noru.utils.FileUtil.java
     * @date :2015. 1. 17. ���� 2:07:29
     * @version :1.0
     * @author :Administrator
     * @Method :getFileChannel
     * @Return :FileChannel
     */
    private static final FileChannel getFileChannel(String fileName)
    {
        Path fp = FILE_SYSTEMS.getPath(escapeFileName(fileName));

        FileChannel outChannel = null;

        try
        {
            outChannel = FileChannel.open(fp, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE));

        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return outChannel;
    }

    /**
     * <PRE>
     * ���ϸ��� ���ڸ��� ��´�.
     * </PRE>
     *
     * @location org.noru.utils.FileUtil.java
     * @cdate 2013. 9. 10.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param fileName
     * @return
     */
    private static final String getFileExt(String fileName)
    {
        if (!"".equals(fileName) && fileName.lastIndexOf(".") > 0)
        {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else
        {
            return "";
        }
    }

    /**
     * file ��θ� �ָ� �ش� ������ ���� ã�Ƽ� List�� ��ȯ�Ѵ�.
     *
     * @optitle
     * @cdate 2011. 5. 27.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param fileName
     * @param fileList
     * @throws Exception
     */
    private static final List<String> getFileList(String fileName, boolean searchSubDir)
    {
        if (isEmptyString(fileName))
        {
            return null;
        }

        // OS�� �µ��� ���� �н��� �����Ѵ�.
        fileName = convertFilePathFitOs(fileName);

        File file = getFile(fileName);

        // �������� ������ �ƹ� �۾��� ���� �ʴ´�.
        if (!file.exists())
        {
            return null;
        }

        List<String> fileList = new ArrayList<String>();
        // ���� directory�� �ƴϸ� �׳� ������. directory�� ������� ������ �Ǵ��Ѵ�.
        if (file.isDirectory())
        {
            String path = getPath(fileName);

            // Directory �ִ� ��� File�� �����ͼ� ����۾��Ѵ�
            String[] filelist = file.list();
            for (String element : filelist)
            {
                String subFileName = path + element;

                // ���丮 �϶��� �ٽ� ���ȣ���� ���� ���� ���丮�� �������Ѵ�.
                File subDir = getFile(subFileName);
                if (subDir.isDirectory() && searchSubDir)
                {
                    List<String> subFileList = getFileList(subFileName, searchSubDir);
                    if (subFileList != null)
                    {
                        fileList.addAll(subFileList);
                    }
                } else
                {
                    fileList.add(subFileName);
                }
            }
        } else
        {
            fileList.add(fileName); // �׳� �����̸� �Ѱ��� �����Ѵ�.
        }

        return fileList;
    }

    /**
     * <PRE>
     * ���ϸ��� ������ �̴�. �� �׳� ���� ���� �ʴ� ������ ��Ī�� ���� �ö����δ�.
     * ���ϸ��� ��´�. �� path�� ������ ���ϸ��̴�.
     * D:/noru/temp.txt.org -> temp.txt.org \\�� / ��path�� ������ ��� �ü��� �ִ�.
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 5. 27.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param fileName
     * @return
     */
    private static final String getFileName(String fileName, boolean removeExt)
    {
        if (isEmptyString(fileName))
        {
            return "";
        }
        // \t \s ��� Ư�����ڰ������� �Ϲ����� ���� ������ �νĸ���
        fileName = escapeFileName(fileName);
        if (fileName.substring(fileName.length() - 1).equals("/") || fileName.substring(fileName.length() - 1).equals("\\"))
        {
            fileName = fileName.substring(0, fileName.length() - 1);
        }
        boolean isNotWin = fileName.lastIndexOf("/") > fileName.lastIndexOf("\\");
        // ���� /�� ���Ե� ����̰� ������ ��α����ڰ� ������ \\ �����ڰ� �ƴҰ��
        if (fileName.lastIndexOf("/") > 0 && isNotWin)
        {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        } else if (fileName.lastIndexOf("\\") > 0)
        {
            fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
        }

        if (removeExt)
        {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
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

        if (isEmptyString(fileName))
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

    private static final int getLength(Object input)
    {
        String inputs = toString(input);
        int strLength = 0;

        for (int i = 0; i < inputs.length(); i++)
        {
            char oneChar = inputs.charAt(i);

            if (oneChar > 127)
            {
                strLength += 2;
            } else
            {
                strLength++;
            }
        }
        return strLength;
    }

    private static final long getLong()
    {
        return UUID.randomUUID().getLeastSignificantBits() * -1;
    }

    private static final String getPadVal(String value, String padValue, int length)
    {
        if (value == null)
        {
            return "";
        }
        int orglength = getLength(toString(value));
        int padlength = getLength(padValue);
        int totPadlength = 0;
        if (orglength > length)
        {
            return "";
        }
        String sumPadValue = "";
        for (int i = 0; i < (length - orglength) / padlength; i++)
        {
            sumPadValue += padValue;
            totPadlength += padlength;
        }
        if (padlength > totPadlength)
        {

        }
        return sumPadValue;
    }

    /**
     * <PRE>
     * ������ directory �ӿ��� �ұ��ϰ� \ �� / �� �ȳ����°�� �ǳ��� \�� / ��  �ٿ� �ش�.
     * ������ ���� ���� ���� ���� ���� �뵵
     * </PRE>
     *
     * @location org.noru.utils.FileUtil.java
     * @cdate 2014. 7. 25.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param fileName
     * @return
     */
    private static final String getPath(String fileName)
    {
        fileName = convertFilePathFitOs(fileName);

        // �̸��� ���丮 / \ �� ������� ���� Ȯ���ڸ� �Ⱥٿ���
        if (!fileName.substring(fileName.length() - 1).equals("/") && !fileName.substring(fileName.length() - 1).equals("\\"))
        {
            return fileName + File.separator;
        }
        return fileName;
    }

    private static final int indexOf(Pattern pattern, String str)
    {
        Matcher matcher = pattern.matcher(str);
        return matcher.find() ? matcher.start() : -1;
    }

    private static final int indexOf(String pattern, String str)
    {
        return indexOf(Pattern.compile(pattern), str);
    }

    /**
     * ��Ʈ���� ����Ʈ�� ��ȯ�Ѵ�.
     *
     * @optitle
     * @cdate 2011. 5. 27.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param stream
     * @return
     * @throws IOException
     */
    private static final byte[] inputStreamToBytes(InputStream stream)
    {
        if (stream == null)
        {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int read = 0;

        while (read != -1)
        {
            try
            {
                read = stream.read(buffer);
            } catch (IOException e)
            {
                // e.printStackTrace();
                return null;
            }

            if (read > 0)
            {
                baos.write(buffer, 0, read);
            }
        }
        return baos.toByteArray();
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

    private static final boolean isEmptyString(CharSequence value)
    {
        if (value == null || value.length() == 0)
        {
            return true;
        }
        return false;
    }

    private static final boolean isEmptyString(String value)
    {
        if (value == null || value.isEmpty())
        {
            return true;
        }
        return false;
    }

    private static final boolean isExists(String fileName)
    {
        File file = getFile(fileName);
        if (file.exists())
        {
            return true;
        } else
        {
            return false;
        }
    }

    private static final boolean isNotEmpty(String value)
    {
        return !isEmptyString(value);
    }

    /**
     * <PRE>
     *  �Է¹��� ��Ʈ���� �����̸� true �����̸� false�� �����Ѵ�.
     *  ���ڴ� ����������� �ʴ´� ��¥�� ����� ����
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 9. 23.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param str
     * @return
     */
    private static final boolean isNumber(String str)
    {
        if (isEmpty(str))
        {
            return false;
        }
        if (str.charAt(0) == '-' || str.charAt(0) == '+')
        {
            str = str.substring(1);
        }
        return isNumeric(str);
    }

    /**
     * <p>Checks if the CharSequence contains only Unicode digits.
     * A decimal point is not a Unicode digit and returns false.</p>
     *
     * <p>{@code null} will return {@code false}.
     * An empty CharSequence (length()=0) will return {@code false}.</p>
     *
     * <p>Note that the method does not allow for a leading sign, either positive or negative.
     * Also, if a String passes the numeric test, it may still generate a NumberFormatException
     * when parsed by Integer.parseInt or Long.parseLong, e.g. if the value is outside the range
     * for int or long respectively.</p>
     *
     * <pre>
     * StringUtils.isNumeric(null)   = false
     * StringUtils.isNumeric("")     = false
     * StringUtils.isNumeric("  ")   = false
     * StringUtils.isNumeric("123")  = true
     * StringUtils.isNumeric("12 3") = false
     * StringUtils.isNumeric("ab2c") = false
     * StringUtils.isNumeric("12-3") = false
     * StringUtils.isNumeric("12.3") = false
     * StringUtils.isNumeric("-123") = false
     * StringUtils.isNumeric("+123") = false
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if only contains digits, and is non-null
     * @since 3.0 Changed signature from isNumeric(String) to isNumeric(CharSequence)
     * @since 3.0 Changed "" to return false and not true
     */
    public static boolean isNumeric(final CharSequence cs) {
        if (isEmptyString(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
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

    /**
     * <PRE>
     *  1. ����
     *    MapList ������ �ڷḦ �̻ڰ� ����Ѵ�.
     *  2. ó��
     *    �Ʒ��� ���� format���� �����ϰ� �����.
     *    ��-��-��
     *    ��-��-��
     *    �� �� ��
     *    ��-��-��
     * </PRE>
     *
     * @location org.noru.utils.AlignUtil.java
     * @Method listMapToLogString
     * @cdate 2015. 6. 24. ���� 5:20:13
     * @version 1.0
     * @author Administrator
     * @param listMap
     * @return
     */
    private static final String listMapToLogString(List<Map<String, Object>> listMap)
    {
        String rtnStr = "";
        if (listMap == null)
        {
            return rtnStr;
        }
        if (listMap.isEmpty())
        {
            return rtnStr;
        }

        String[][] mapArray = new String[listMap.size() + 1][];
        // �ش��� ����� �ֱ� ���� ����
        String[] header = null;
        for (int i = 0; i < listMap.size(); i++)
        {
            Map<String, Object> map = listMap.get(i);
            if (map == null)
            {
                continue;
            }

            // �ش��� ����� �ش�. �̶� �ش��� keyset�� ũ�Ⱑ ū�� ������ �ȴ�.
            if (header == null)
            {
                header = map.keySet().toArray(new String[0]);
            } else
            {
                if (header.length < map.keySet().size())
                {
                    header = map.keySet().toArray(new String[0]);
                }
            }

            // 120���ڰ� �Ѿ� ���°��� �߶� �ְ� ���Ͱ� ��� ������ �ϳ��� �̾� �ش�
            int valIndex = 0;
            String[] values = new String[map.size()];
            for (Object value : map.values())
            {
                // Tab�� �������� �����Ѵ�.
                String strVal = objectToString(value).replaceAll(REX_TAB, TAB_SPACE);
                strVal = subString(strVal, 120);
                values[valIndex] = strVal;
                valIndex++;
            }
            mapArray[i + 1] = values;
        }
        mapArray[0] = header;

        // ���˵� String�� �����.
        StringBuffer alignedString = new StringBuffer();
        int maxLengthOfArrays = maxLength(mapArray);
        int[] lengthOfColumns = new int[maxLengthOfArrays];

        for (int i = 0; i < mapArray.length; i++)
        {
            if (maxLengthOfArrays > mapArray[i].length)
            {
                mapArray[i] = arrayConcat(mapArray[i], new String[maxLengthOfArrays - mapArray[i].length]);
            }
        }

        // �迭�� �ִ밪 �� �� �����´�
        for (int i = 0; i < maxLengthOfArrays; i++)
        {
            lengthOfColumns[i] = maxLength(i, mapArray); // �迭�� �ִ밪�� ��´�.
        }

        String mainHeadLine = "";

        for (int i = 0; i < lengthOfColumns.length; i++)
        {
            int columnLength = lengthOfColumns[i];
            if (i == 0)
            {
                // ù�� ó�� ���� �÷��� 1���ۿ� ���°��
                if (lengthOfColumns.length == 1)
                {
                    mainHeadLine += TOP_LEFT + rpad("", DASH, columnLength) + TOP_RIGHT;

                } else
                {
                    mainHeadLine += TOP_LEFT + rpad("", DASH, columnLength);
                }

            } else
            {
                if (lengthOfColumns.length - 1 == i)
                {
                    // ��������
                    mainHeadLine += TOP_MIDLE + rpad("", DASH, columnLength) + TOP_RIGHT;
                } else
                {
                    mainHeadLine += TOP_MIDLE + rpad("", DASH, columnLength);
                    // �߰�
                }
            }
        }
        alignedString.append(mainHeadLine + "\n");

        // ���μ��θ� �ٲٸ鼭 ����Ѵ�.
        for (int i = 0; i < mapArray.length; i++)
        {
            for (int j = 0; j < maxLengthOfArrays; j++)
            {
                int columnLength = lengthOfColumns[j];
                String content = objectToString(mapArray[i][j]);

                // ó�� �÷�
                if (j == 0)
                {
                    alignedString.append(VERTICAL);
                }

                // ������ �÷�
                if (j == maxLengthOfArrays - 1)
                {
                    if (mapArray[i][j] == null)
                    {
                        alignedString.append(rpad("", ' ', columnLength));
                    } else
                    {
                        if (isNumber(content))
                        {
                            alignedString.append(lpad(content, ' ', columnLength));
                        } else
                        {
                            alignedString.append(rpad(content, ' ', columnLength));
                        }
                    }
                    alignedString.append(VERTICAL);

                } else
                {

                    if (mapArray[i][j] == null)
                    {
                        alignedString.append(rpad("", ' ', columnLength));
                    } else
                    {
                        if (isNumber(content))
                        {
                            alignedString.append(lpad(content, ' ', columnLength)).append(VERTICAL);
                        } else
                        {
                            alignedString.append(rpad(content, ' ', columnLength)).append(VERTICAL);
                        }
                    }

                    if (lengthOfColumns.length == 1)
                    {
                        mainHeadLine += TOP_LEFT + rpad("", DASH, columnLength) + TOP_RIGHT;
                    }
                }
            }

            alignedString.append(NEW_LINE);

            if (i == 0)
            {
                String tailHeadLine = "";
                for (int k = 0; k < lengthOfColumns.length; k++)
                {
                    int columnLength = lengthOfColumns[k];

                    if (k == 0)
                    {
                        // ù�� ó�� ���� �÷��� 1���ۿ� ���°��
                        if (lengthOfColumns.length == 1)
                        {
                            tailHeadLine += MIDDLE_LEFT + rpad("", DASH, columnLength) + MIDDLE_RIGHT;
                        } else
                        {
                            tailHeadLine += MIDDLE_LEFT + rpad("", DASH, columnLength);
                        }

                    } else
                    {
                        if (lengthOfColumns.length - 1 == k)
                        {
                            // ��������
                            tailHeadLine += CENTER + rpad("", DASH, columnLength) + MIDDLE_RIGHT;
                        } else
                        {
                            tailHeadLine += CENTER + rpad("", DASH, columnLength);
                            // �߰�
                        }
                    }
                }
                alignedString.append(tailHeadLine).append(NEW_LINE);
            }
        }

        // ���� Line �� ����� �ش�
        String lastLine = "";
        for (int i = 0; i < lengthOfColumns.length; i++)
        {
            int columnLength = lengthOfColumns[i];

            if (i == 0)
            {
                // ù�÷��� �÷��� 1���ۿ� ������
                if (lengthOfColumns.length == 1)
                {
                    lastLine += BUTTOM_LEFT + rpad("", DASH, columnLength) + BUTTOM_RIGHT;
                } else
                {
                    lastLine += BUTTOM_LEFT + rpad("", DASH, columnLength);
                }

            } else
            {
                if (lengthOfColumns.length - 1 == i)
                {
                    // ��������
                    lastLine += BUTTOM_MIDDLE + rpad("", DASH, columnLength) + BUTTOM_RIGHT;
                } else
                {
                    // �߰�
                    lastLine += BUTTOM_MIDDLE + rpad("", DASH, columnLength);
                }
            }
        }
        alignedString.append(lastLine);

        return alignedString.toString();
    }

    private static final String lpad(Object value, char padValue, int length)
    {
        return lpad(value, padValue + "", length);
    }

    private static final String lpad(Object value, String padValue, int length)
    {
        String tranVal = objectToString(value).replaceAll(REX_TAB, TAB_SPACE).replaceAll(REX_NEW_LINE, SPACE);
        return getPadVal(tranVal, padValue, length) + tranVal;
    }

    private static final void makeDir(String dirName) throws IOException
    {
        File dir = getFile(dirName);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
    }

    private static final File makeFile(String fileName) throws IOException
    {
        File file = getFile(fileName);
        if (!file.getParentFile().exists())
        {
            file.getParentFile().mkdirs();
        }
        if (!file.exists())
        {
            file.createNewFile();
        }
        return file;
    }

    /**
     * <PRE>
     *  �α׸� ������ ������ �����.
     * </PRE>
     *
     * @location org.noru.utils.java
     * @cdate 2013. 9. 10.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param comment
     * @param size
     * @param chars
     * @return
     */
    private static final String makeLogSentence(String comment, int size, String chars)
    {
        // �ձ��� �е��� ũ�⸦ ���Ѵ�
        int paddingSize = (size - getLength(comment)) / 2;

        String returnString = lpad("", chars, paddingSize) + comment + " " + rpad("", chars, paddingSize);
        String addSpace = lpad("", " ", size - returnString.getBytes().length);
        return lpad("", chars, paddingSize) + " " + comment + addSpace + rpad("", chars, paddingSize);
    }

    private static void mapValueToString(int depth, StringBuilder sb, Object value)
    {
        if (value instanceof Map)
        {
            sb.append(toPrintString(value, depth));
            sb.append(NEW_LINE);
        } else if (value instanceof Iterable)
        {
            sb.append(NEW_LINE);
            Iterator it = ((Iterable) value).iterator();
            if (!it.hasNext())
            {
                return;
            }
            while (it.hasNext())
            {
                sb.append(toPrintString(it.next(), depth));
                sb.append(NEW_LINE);
            }
        } else if (value instanceof Enumeration)
        {
            sb.append(NEW_LINE);
            Enumeration em = (Enumeration) value;
            if (!em.hasMoreElements())
            {
                return;
            }
            while (em.hasMoreElements())
            {
                sb.append(toPrintString(em.nextElement(), depth));
                sb.append(NEW_LINE);
            }
        } else if (value instanceof ByteBuffer)
        {
            while (((ByteBuffer) value).hasRemaining())
            {
                sb.append((char) ((ByteBuffer) value).get());
            }
            sb.append(NEW_LINE);
        } else
        {
            sb.append(objectToString(value));
            sb.append(NEW_LINE);
        }
    }

    /**
     * �ش� index�� �ִ� ���� ���̰� ���� ū ���� ����ũ�� ��ȯ
     *
     * @optitle
     * @cdate 2011. 6. 9.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param <T>
     * @param lc
     * @param src
     * @return
     */
    private static final <T> int maxLength(int lc, T[]... src)
    {
        int length = 0;
        for (T[] tmp : src)
        {
            if (tmp[lc] != null && length < getLength(tmp[lc]))
            {
                length = getLength(tmp[lc]); // tmp[lc].toString().getBytes(Charset.forName("EUC-KR")).length;
            }
        }
        return length;
    }

    private static final <T> int maxLength(T[] src)
    {
        int length = 0;
        for (T tmp : src)
        {
            if (tmp == null)
            {
                continue;
            }
            if (length < getLength(tmp))
            {
                length = getLength(tmp);
            }
        }
        return length;
    }

    /**
     * �迭���� ũ�⸦ ���Ѵ�.
     *
     * @optitle
     * @cdate 2011. 6. 8.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param <T>
     * @param src
     * @return
     */
    private static final <T> int maxLength(T[]... src)
    {
        int length = 0;
        for (T[] tmp : src)
        {
            if (length < tmp.length)
            {
                length = tmp.length;
            }
        }
        return length;
    }

    private static final void mergeFile(String fileName, List<String> fileNms) throws IOException
    {
        // ���ϸ��� �����Ѵ�
        fileName = convertFilePathFitOs(fileName);

        // ���������� �����
        delete(fileName);

        FileOutputStream fos = new FileOutputStream(fileName, true);

        for (String fileNm : fileNms)
        {
            fos.write(fileToBytes(fileNm));
            fos.flush();
        }

        fos.close();
        fos = null;
    }

    private static final byte[] objectToBytes(Object input)
    {
        ByteArrayOutputStream ostream = null;
        ObjectOutput out = null;
        byte[] bytes = null;
        try
        {
            ostream = new ByteArrayOutputStream();
            out = new ObjectOutputStream(new BufferedOutputStream(ostream));
            out.writeObject(input);
            out.flush(); // ���� ��Ʈ���� ��� �ѱ��
            bytes = ostream.toByteArray();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        closeResource(out); // ���⼭ �ݴ´�
        closeResource(ostream);

        return bytes;
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

    private static void objectToString(Object input, int depth, StringBuilder sb)
    {
        if (input instanceof Map)
        {
            sb.append(toPrintString(input, depth));
            sb.append(NEW_LINE);
        } else if (input instanceof List) // ArrayList < List < Collection < Iterable
        {
            sb.append(makeLogSentence(LEFT_WAP + "List size:" + ((List) input).size() + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
            for (Object object : (List) input)
            {
                sb.append(toPrintString(object, depth));
                sb.append(NEW_LINE);
            }
            sb.append(makeLogSentence(LEFT_WAP + "End List" + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
        } else if (input instanceof Set)
        {
            sb.append(makeLogSentence(LEFT_WAP + "Set size:" + ((Set) input).size() + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
            for (Object object : (Set) input)
            {
                sb.append(toPrintString(object, depth));
                sb.append(NEW_LINE);
            }
            sb.append(makeLogSentence(LEFT_WAP + "End Set" + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
        } else if (input instanceof Queue)
        {
            sb.append(makeLogSentence(LEFT_WAP + "Queue size:" + ((Queue) input).size() + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
            for (Object object : (Queue) input)
            {
                sb.append(toPrintString(object, depth));
                sb.append(NEW_LINE);
            }
            sb.append(makeLogSentence(LEFT_WAP + "End Queue" + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
        } else if (input instanceof Collection)
        {
            sb.append(makeLogSentence(LEFT_WAP + "Collection size:" + ((Collection) input).size() + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
            for (Object object : (Collection) input)
            {
                sb.append(toPrintString(object, depth));
                sb.append(NEW_LINE);
            }
            sb.append(makeLogSentence(LEFT_WAP + "End Collection" + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
        } else if (input instanceof Iterable)
        {
            Iterator it = ((Iterable) input).iterator();
            if (!it.hasNext())
            {
                return;
            }
            int i = 0;
            sb.append(makeLogSentence(LEFT_WAP + "Iterable size:" + i + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
            while (it.hasNext())
            {
                sb.append(toPrintString(it.next(), depth));
                sb.append(NEW_LINE);
                i++;
            }
            sb.append(makeLogSentence(LEFT_WAP + "End Iterable" + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
        } else if (input instanceof Enumeration)
        {
            Enumeration em = (Enumeration) input;
            if (!em.hasMoreElements())
            {
                return;
            }
            int i = 0;
            sb.append(makeLogSentence(LEFT_WAP + "Enumeration size:" + i + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
            while (em.hasMoreElements())
            {
                sb.append(toPrintString(em.nextElement(), depth));
                sb.append(NEW_LINE);
                i++;
            }
            sb.append(makeLogSentence(LEFT_WAP + "End Enumeration" + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
        } else if (input instanceof ByteBuffer)
        {
            while (((ByteBuffer) input).hasRemaining())
            {
                sb.append((char) ((ByteBuffer) input).get());
            }
        } else if (input instanceof Object[])
        {
            sb.append(makeLogSentence(LEFT_WAP + "Objects size:" + ((Object[]) input).length + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
            for (Object object : (Object[]) input)
            {
                sb.append(toPrintString(object, depth));
                sb.append(NEW_LINE);
            }
            sb.append(makeLogSentence(LEFT_WAP + "End Objects" + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
        } else
        {
            sb.append(objectToString(input));
            sb.append(NEW_LINE);
        }
    }

    /**
     * <PRE>
     *  ���� �ּ� --, // �� �����ؼ� NEW_LINE ������ �׸��� NEW_LINE���� ��ü
     *  ������ �ּ��� ��� �Ʒ��� ���� 800 �ڰ� �Ѿ ���忡�� ������ ����.
     *  input = input.replaceAll(REX_MULTILINE_COMMENT, "");
     *
     * </PRE>
     *
     * @optitle
     * @cdate 2013. 8. 20.
     * @version 1.0
     * @author Administrator
     * @tag
     * @param input
     * @param isJava
     * @return
     */
    private static final String removeComment(String input, boolean isJava)
    {
        for (; input.indexOf("/*") > -1;)
        {
            int startIndx = input.indexOf("/*");
            int endIndx = input.indexOf("*/", startIndx) + 2;

            if (startIndx < endIndx)
            {
                input = input.replace(input.substring(startIndx, endIndx), "");
            } else
            {
                break;
            }
        }
        if (isJava)
        {
            // �Ʒ� ���� �������� ����.
            input = input.replaceAll(REX_JAVA_COMMENT, "");
        } else
        {
            input = input.replaceAll(REX_SQL_COMMENT, "");
        }
        return input;
    }

    /**
     * <PRE>
     * UTF-8�� ����� File���Ŀ��� Bom�� ���� ���ش�.
     * </PRE>
     *
     * @optitle
     * @cdate 2012. 9. 5.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param input
     * @return
     */
    private static final String removeUTF8BOM(String input)
    {
        if (input != null && input.length() >= 1)
        {
            if (input.startsWith(UTF8_BOM))
            {
                input = input.substring(1);
            }
        }
        if (input == null)
        {
            input = "";
        }
        return input;
    }

    private static final String rpad(Object value, char padValue, int length)
    {
        return rpad(value, padValue + "", length);
    }

    private static final String rpad(Object value, int length, String padValue)
    {
        return rpad(value, padValue, length);
    }

    private static final String rpad(Object value, String padValue, int length)
    {
        if (length > 0)
        {
            String tranVal = objectToString(value).replaceAll(REX_TAB, TAB_SPACE).replaceAll(REX_NEW_LINE, SPACE);
            return tranVal + getPadVal(tranVal, padValue, length);
        } else
        {
            return objectToString(value);
        }
    }

    /**
     * <PRE>
     * 2Byte�� ������ subString
     * </PRE>
     *
     * @location org.noru.utils.java
     * @cdate 2014. 11. 3.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param input
     * @param length
     * @return
     */
    private static final String subString(String input, int length)
    {
        int strLength = 0;
        int subLength = 0;

        subLength = input.length();

        for (int i = 0; i < input.length(); i++)
        {
            if (strLength > length)
            {
                subLength = i - 1; // ���� ���ڸ�ŭ�� ũ�⸦ ����
                break;
            }
            String oneChar = input.substring(i, i + 1);
            if (oneChar.matches(REX_2BYTE_CHAR)) // 2byte���ڷ� �ν�
            {
                strLength += 2;
            } else
            {
                strLength++;
            }
        }
        return input.substring(0, subLength);
    }

    private static final String toPrintString(Object input)
    {
        return toPrintString(input, 0);
    }

    private static String toPrintString(Object input, int depth)
    {
        StringBuilder sb = new StringBuilder();
        if (input instanceof Map)
        {
            Object key;
            Object value;
            Object[] keys = ((Map) input).keySet().toArray();
            sb.append(NEW_LINE);
            // sb.append(makeLogSentence(LEFT_WAP + "Map size:" + keys.length + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
            sb.append("---------------------------------------------------------------------------------").append(NEW_LINE);
            sb.append("Key                                     | Value").append(NEW_LINE);
            sb.append("----------------------------------------+----------------------------------------").append(NEW_LINE);
            int keylength = 40;
            for (Object key2 : keys)
            {
                key = key2; // String ���� �ڵ� ĳ���õȴ�.
                value = ((Map) input).get(key);
                sb.append(rpad(objectToString(key), " ", keylength));
                sb.append("| ");
                // 40 ���� �ѹ��� addSpace �Ѵ�
                mapValueToString(10, sb, value);
            }
            sb.append("---------------------------------------------------------------------------------").append(NEW_LINE);
            // sb.append(makeLogSentence(LEFT_WAP + "End Map" + RIGHT_WAP, LOG_STRING_SIZE, "-")).append(NEW_LINE);
        } else
        {
            // �����δ� 1�� ������ �Ǵ°��̴�. �ֳ��ϸ� addTabs �� ����� �ٽ� addTabs �ϱ� �����̴�
            objectToString(input, 1, sb);
        }
        String returnString = addTabs(sb.toString(), depth);
        return returnString;
    }

    private static final String toString(Object input)
    {
        return objectToString(input);
    }

    private static final String[] toStringArray(String input)
    {
        return input.split(REX_NEW_LINE);
    }

    /**
     * <PRE>
     *  1. ����
     *      ���� ä�ο� byte[]�� ����
     *  2. ó��
     *      DML�� ���� allocateDirect�� ����
     *      ����Ʈ�� ũ�⸸ŭ for�� ���鼭 limit �� ���� flip�ϰ� ����
     * </PRE>
     *
     * @location org.noru.utils.FileUtil.java
     * @Method writeBytesToFileChannel
     * @cdate 2015. 5. 27. ���� 1:53:05
     * @version 1.0
     * @author Administrator
     * @param input
     * @param outChannel
     * @return
     */
    private static final boolean writeBytesToFileChannel(byte[] input, FileChannel outChannel)
    {
        ByteBuffer buf = ByteBuffer.allocateDirect(BUFFER_SIZE);
        try
        {
            for (byte ch : input)
            {
                buf.put(ch);
                if (buf.position() == buf.limit())
                {
                    buf.flip();
                    outChannel.write(buf);
                    buf.rewind();
                }
            }
            buf.flip();
            outChannel.write(buf);
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        buf.clear(); // �����.
        return true;
    }

    /**
     * byte�� �޾Ƽ� file�� ����.
     *
     * @param input
     * @param fileName
     * @throws IOException
     */
    public static final boolean byteWriteFile(byte[] input, String fileName)
    {
        FileOutputStream fos = null;
        try
        {
            makeFile(fileName);
            File file = getFile(fileName);
            fos = new FileOutputStream(file);
            fos.write(input);

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        closeResource(fos);

        return true;
    }

    public static final String convertFilePathFitUnix(String fileName)
    {
        return escapeFileName(fileName).replaceAll("\\\\", "/");
    }

    public static final boolean copyFile(String source, String target)
    {
        // ���¹� ������ ��� �´�
        File tarFile = new File(target);

        // ��Ʈ���� ����
        File sourceFile = getFile(source);

        return copyFile(sourceFile, tarFile);
    }

    public static final <T> T fileToObject(File file)
    {
        T returnVal = null;
        ObjectInput in = null;

        try
        {
            in = new ObjectInputStream(new ByteArrayInputStream(fileToBytes(file)));
            returnVal = (T) in.readObject();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        closeResource(in);

        return returnVal;
    }

    /**
     * ���Ϸ� ����� �ڹ� ��ü�� �ҷ� ���� �޼ҵ��̴�.
     *
     * @param fileName
     *            : ��ü�� ����� ������ ���
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @optitle
     * @cdate 2011. 7. 15.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param fileName
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static final <T> T fileToObject(String fileName)
    {
        T returnVal = null;
        ObjectInput in = null;

        try
        {
            in = new ObjectInputStream(new ByteArrayInputStream(fileToBytes(fileName)));
            returnVal = (T) in.readObject();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        closeResource(in);

        return returnVal;
    }

    public static final String[] fileToStringArray(String fileName) throws IOException
    {
        return fileToStringArray(fileName, 0); // 0�� �ָ� ��� ���ϸ���Ʈ�� ����.
    }

    /**
     * <PRE>
     * </PRE>
     *
     * @location org.noru.utils.FileUtil.java
     * @Method findStr
     * @cdate 2015. 10. 1. ���� 10:01:17
     * @version 1.0
     * @author Administrator
     * @param dirName
     * @param findStrs
     * @return
     */
    public static final List<String> findString(String dirName, String[] findStrs)
    {
        if (isEmptyString(dirName))
        {
            return null;
        }

        if (findStrs == null)
        {
            return null;
        }

        if (findStrs.length == 0)
        {
            return null;
        }

        int maxStr = maxLength(findStrs);

        if (maxStr == 0)
        {
            return null;
        }

        List<String> results = new ArrayList<String>();
        List<String> fileNames = getFileList(dirName, true);
        int maxFileNames = maxLength(fileNames.toArray(new String[] {}));

        for (String fileName : fileNames)
        {
            String fileContents = removeComment(fileToString(fileName), true);
            String[] lines = toStringArray(fileContents);
            int lineNum = 1;

            for (String line : lines)
            {
                lineNum++;

                if (isEmptyString(line))
                {
                    continue;
                }

                for (String findStr : findStrs)
                {
                    // ã�� ���ڰ� ������ ����
                    if (isEmptyString(findStr))
                    {
                        continue;
                    }

                    int findIndx = indexOf(findStr, line);

                    if (findIndx > -1)
                    {
                        results.add(rpad(findStr, maxStr, " ") + " : find At fileName -> " + rpad(fileName, maxFileNames, " ") + " LineNum -> " + rpad(lineNum, 4, " ") + " ColNum -> " + findIndx);
                    }
                }
            }
        }

        return results;
    }

    public static final String inputStreamToFile(InputStream inputStream, String fileName)
    {
        FileOutputStream fos = null;
        try
        {
            // ������ ���� �Ҷ��� ���ο� ���Ϸ� ����. �� �ӽ� ���ϸ��� �����
            if (isExists(fileName))
            {
                fileName = getFilePath(fileName) + getFileName(fileName, true) + "_" + getLong() + "." + getFileExt(fileName);
            }

            File file = makeFile(fileName);
            fos = new FileOutputStream(file);
            byte[] buf = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buf)) != -1)
            {
                fos.write(buf, 0, bytesRead);
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        closeResource(fos);

        return fileName;
    }

    public static final String inputStreamToString(InputStream stream) throws IOException
    {
        return new String(inputStreamToBytes(stream)); // ��Ʈ���� �о
    }

    public static final String inputStreamToString(InputStream stream, Charset charset)
    {
        return new String(inputStreamToBytes(stream), charset); // ��Ʈ���� �о
    }

    /**
     * <PRE>
     *  ���� ���� �� ��� ������ ���� �ش�
     *  �Ʒ��� ���� �μ��� �ָ� C:\temp\ ������ temp.txt�� ������
     *  temp.txt�� �̸��� �� ��� ������ ���� �ش�.
     *
     *  Ex)
     *  mergeFile("C:\temp\temp.txt")
     *
     *     C:\temp\temp.txt.1
     *     C:\temp\temp.txt.2
     *     C:\temp\temp.txt.3
     *     C:\temp\temp.txt.4
     *     C:\temp\temp.txt.5
     *     C:\temp\temp.txt.6
     *     C:\temp\temp.txt.7
     *     C:\temp\temp.txt.8
     *     C:\temp\temp.txt.9
     *
     * </PRE>
     *
     * @location org.noru.utils.FileUtil.java
     * @Method mergeFile
     * @cdate 2015. 9. 9. ���� 5:24:45
     * @version 1.0
     * @author Administrator
     * @param fileName
     */
    public static final void mergeFile(String fileName) throws IOException
    {
        fileName = convertFilePathFitOs(fileName);

        String dir = getFilePath(fileName);
        List<String> fileNms = getFileList(dir, false);

        List<String> fileNames = new ArrayList<String>();

        for (String fileNm : fileNms)
        {
            // ���ϸ��� ���°��� ����
            if (!fileNm.contains(fileName))
            {
                continue;
            }
            fileNames.add(fileNm);
        }

        mergeFile(fileName, fileNames);
    }

    public static final void moveFile(String oldFileName, String newFileName)
    {
        File oldFile = getFile(oldFileName);
        if (oldFile.exists())
        {
            File fileRename = getFile(newFileName);
            oldFile.renameTo(fileRename);
        } else
        {
            System.err.println("File not found ! " + oldFileName);
        }
    }

    /**
     * <PRE>
     * �ڹ��� ��ü�� ���Ϸ� �����ϴ� �Լ��̴�.
     * ����ÿ��� out ByteArrayOutputStream �� byte�� output�� �ϴ� �Լ� �̴�.
     * ���߿� ������� �ִ�. �� output�� ����� ������ �ִ�.
     *
     * </PRE>
     *
     * @optitle
     * @cdate 2011. 7. 15.
     * @version 1.0
     * @author �ڼ���
     * @tag
     * @param input
     * @param fileName
     * @throws IOException
     */
    public static final boolean objectToFile(Object input, String fileName)
    {
        try
        {
            // ��ü�� byte[]�� �����Ѵ�.
            byte[] serializedObject = objectToBytes(input);

            if (serializedObject != null)
            {
                File file = getFile(getFilePath(fileName));
                if (!file.exists())
                {
                    makeDir(getFilePath(fileName));
                }
                bytesToFile(serializedObject, fileName);
            } else
            {
                return false;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static final void print(List<Map<String, Object>> object)
    {
        if (object == null)
        {
            return;
        }
        // �ҷ��� �޼ҵ带 ��´�.
        StackTraceElement st[] = new Throwable().fillInStackTrace().getStackTrace();
        String runMethod = "";
        if (st != null && st.length > 1)
        {
            StackTraceElement ste = st[1];
            runMethod = ste.getClassName() + "." + ste.getMethodName() + "(Line:" + ste.getLineNumber() + ")";
        } else
        {
            runMethod = st[0].getClassName() + "." + st[0].getMethodName() + "(Line:" + st[0].getLineNumber() + ")";
        }

        String prtStr = listMapToLogString(object);

        System.out.println(runMethod + "\n: " + toPrintString(prtStr)); // ����Ѵ�.
    }

    public static final void print(Object object, Object... objects)
    {
        if ("N".equals(System.getProperty(PRINT_YN)))
        {
            return;
        }

        if (object == null && objects == null)
        {
            return;
        }
        // �ҷ��� �޼ҵ带 ��´�.
        StackTraceElement st[] = new Throwable().fillInStackTrace().getStackTrace();
        String runMethod = "";
        if (st != null && st.length > 1)
        {
            StackTraceElement ste = st[1];
            runMethod = ste.getClassName() + "." + ste.getMethodName() + "(Line:" + ste.getLineNumber() + ")";
        } else
        {
            runMethod = st[0].getClassName() + "." + st[0].getMethodName() + "(Line:" + st[0].getLineNumber() + ")";
        }

        // �α׸� ����Ѵ�.
        System.out.println(runMethod + ":\n" + toPrintString(arrayInsert(objects, object, 0))); // ����Ѵ�.

    }

    public static final void splitFile(String fileName, int splitSize) throws IOException
    {
        fileName = convertFilePathFitOs(fileName);

        String newFileName;
        FileOutputStream filePart;
        int nChunks = 1;
        int read = 0;
        int readLength = splitSize;

        byte[] byteChunkPart;

        InputStream inputStream = fileToInputStream(fileName);
        int fileSize = inputStream.available();

        // �е��� ����
        int padLen = getDigit(fileSize / splitSize);

        while (fileSize > 0)
        {
            // ������
            if (fileSize <= readLength)
            {
                readLength = fileSize;
            }

            // inputStream���� ������ �ڸ��� ũ�⸸ŭ �д´�
            byteChunkPart = new byte[readLength];
            read = inputStream.read(byteChunkPart, 0, readLength);

            // ������ŭ �����Ѵ�
            fileSize -= read;

            assert read == byteChunkPart.length;

            newFileName = fileName + "." + lpad(nChunks, "0", padLen);

            filePart = new FileOutputStream(getFile(newFileName));
            filePart.write(byteChunkPart);
            filePart.flush();
            filePart.close();

            byteChunkPart = null;
            filePart = null;

            nChunks++;
        }

        inputStream.close();

    }

    public static final boolean stringToFile(CharSequence input, String fileName)
    {
        try
        {
            bytesToFile(input.toString().getBytes(), fileName);
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * <PRE>
     *  �ι��� ���̿� ���� ���ڸ� ã�´�
     * </PRE>
     *
     * @location org.noru.utils.ReplaceUtil.java
     * @Method findBetweenStr
     * @cdate 2015. 10. 1. ���� 4:37:42
     * @version 1.0
     * @author Administrator
     * @param input
     * @param start
     * @param end
     * @return
     */
    public static final String findBetweenStr(String input, String start, String end)
    {
        int startIndx = input.indexOf(start);
        int endIndx = input.indexOf(end, startIndx) + end.length(); //�����ε����� ���ۺ��� Ŀ�ߵȴ�
        return input.substring(startIndx, endIndx);
    }

    public static final List<String> findBetweenStrs(String input, String start, String end)
    {
        List<String> retStrs = new ArrayList<String>();

        for (; input.indexOf(start) > -1;)
        {
            String findStr = findBetweenStr(input, start, end);
            retStrs.add(findStr);

            input = input.replace(findStr, "");
        }

        return retStrs;
    }

    public static void main(String[] args)
    {
        print(findString("C:\temp", new String[] { "FileUtil", "Constants" }));
    }
}
